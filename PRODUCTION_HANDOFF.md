# Production source handoff — release candidate, not a certified release

Source baseline: sw7425/Staging commit 5ffda60fedc0a8cd90227d995b0886caa3f439a8. This copy removes staging-only QA/broadcast commands, fixture data and activation hooks. It does not contain a database export, populated environment file, Firebase server credentials, private signing key, signed APK or a data-transfer script.

Target website: https://www.kpn.com.ng

## Included changes

State announcements reach verified, active state executive team members only. General remains the all-member scope. Role dashboard Quick Actions use four concise shortcuts plus More actions; surrounding dashboard sections are preserved. Verified report-capability, legacy-role-link, currency-format, email-failure and static-cache fixes are included.

## Before deploying

1. Back up the existing production PostgreSQL database, keep the currently deployed source/revision and confirm the backup can be restored. No backup or recovery test was performed against production here.
2. Keep ALL existing production environment values, database, Cloudinary assets, Telegram bot/channel, email configuration, session secret and release signing key. Do not copy any staging environment settings or test accounts. Do not import staging data.
3. Set/confirm KPN_ENV=production, DEBUG=False, SITE_URL=https://www.kpn.com.ng and PYTHON_VERSION=3.11.15. The environment example contains blanks/placeholders only; do not replace your live values with them.
4. Run the read-only database/configuration preflight against your existing database: python manage.py check_release --database. Stop on errors; resolve duplicate seats or missing jurisdiction before applying migrations.
5. Recommended Render build: bash scripts/render-build.sh. Recommended start: bash scripts/render-start.sh. The start script runs guarded, advisory-lock-serialized migrations before Gunicorn. Review the migrations before deployment; they are not a database backup.
6. After deployment, verify https://www.kpn.com.ng/health/ready/, login/approval flows, each role's dashboard, General/State audience restrictions, uploads, actual email delivery and device notifications.

## Important limits

The staging General test delivered one inbox notification to each of 44 eligible staging members, with no missing/unexpected/duplicate recipients. Eighty-four HTTP receipt/content checks passed across 42 synthetic accounts. This is not production receipt evidence.

Staging has no registered Android devices and no Firebase server credential configured, so physical native notification delivery is unverified. The user has the production app only. Production email/FCM configuration was not inspected or changed; preserve its current settings. Staging SMTP is blocked by its hosting setup; bounded failure handling is not a successful mail-delivery test.

Screenshot inspection was unavailable; automated layout checks do not prove a completed visual review. Backup/restore, rollback and current physical-device acceptance remain release gates.

## Native Android source

Kpn_android is SOURCE, not a new installed or signed APK. Its production API default is https://www.kpn.com.ng/api/v1/. Keep the existing application ID and signing key when updating the production app. Confirm the highest published version code before creating a release; do not assume the bundled version code is higher than your installed release. Firebase Android client configuration is not a Firebase Admin service-account credential. The included historical PEM certificate is PUBLIC verification material, not a signing key.

## Packaging validation

The cleaned package passed Django system checks and migration-consistency dry-run. Its retained backend suite completed 109 tests: 107 passed, two PostgreSQL-only concurrency tests skipped on local SQLite/Python 3.13. This does not certify production migrations or physical-device behavior.
