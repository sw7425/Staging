#!/usr/bin/env bash
set -euo pipefail
python - <<'PYCODE'
import os
required = ['SESSION_SECRET','DATABASE_URL','CLOUDINARY_CLOUD_NAME','CLOUDINARY_API_KEY','CLOUDINARY_API_SECRET','TELEGRAM_BOT_TOKEN','TELEGRAM_WEBHOOK_SECRET','TELEGRAM_CHANNEL_ID','TELEGRAM_BOT_USERNAME']
missing = [k for k in required if not os.environ.get(k, '').strip()]
if missing: raise SystemExit('Missing staging settings: ' + ', '.join(missing))
if os.environ.get('SITE_URL', '').rstrip('/') != 'https://kpn-staging.onrender.com': raise SystemExit('Set the staging SITE_URL explicitly.')
if os.environ['TELEGRAM_CHANNEL_ID'] == '-1002364023770' or os.environ['TELEGRAM_BOT_USERNAME'].lstrip('@').lower() == 'kpnkebbibot': raise SystemExit('Use a separate staging Telegram channel and bot.')
if os.environ.get('KPN_ENV') != 'production' or os.environ.get('DEBUG', '').lower() != 'false': raise SystemExit('Staging uses production-shaped checks: KPN_ENV=production and DEBUG=False.')
PYCODE
bash scripts/render-build.sh
