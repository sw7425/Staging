#!/usr/bin/env bash
set -euo pipefail
# Works without Render's paid pre-deploy feature. A PostgreSQL advisory lock
# serializes overlapping instance startups before Gunicorn starts.
python manage.py release_migrate
# Explicit opt-in, exact-service-guarded, one-time synthetic QA initialization.
if [[ "${KPN_STAGING_QA_BOOTSTRAP:-}" == "kpn-staging-fixtures-v1" ]]; then
  python manage.py bootstrap_staging_qa --apply
fi
# Explicitly approved, exact-service-guarded General-only receipt probe.
if [[ "${KPN_STAGING_GENERAL_PROBE:-}" == "kpn-staging-general-receipt-v1" ]]; then
  python manage.py probe_staging_general_announcement --broadcast --verify-http
fi
exec gunicorn KPN.wsgi:application --bind "0.0.0.0:${PORT:-8000}" --workers "${WEB_CONCURRENCY:-3}" --timeout 60 --access-logfile - --error-logfile -
