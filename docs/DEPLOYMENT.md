# Deploying the KPN release candidate

## 1. Prepare the existing service and recovery point
1. Record the current Git revision, Render commands/environment and highest Android version code. Preserve the current signing identity.
2. Create a Neon recovery point/backup before migration. Restore it into a separate restricted database and confirm member, leadership, content and archive-related relationships. Never test restore against production.
3. Record Cloudinary folder/settings and retained asset references. Database backups do not back up Cloudinary files. Agree retention, deletion and restore procedures separately.
4. First deploy this revision to a staging Render service using a separate Neon database and Cloudinary test scope. Do not run synthetic seeds against production.

## 2. Configure privately
Use `.env.production.example` as the list of variables, not as a populated source file. Set values in Render's private Environment/Secret Files controls. Keep the existing securely managed `SESSION_SECRET`; if a known fallback was used, plan an explicit rotation and reauthentication.

Required: `KPN_ENV=production`, `DEBUG=False`, canonical HTTPS `SITE_URL`, private `SESSION_SECRET`, Neon PostgreSQL `DATABASE_URL` with TLS, the three Cloudinary credentials, real Lockally email settings (or explicitly configured SMTP), and Telegram bot/webhook secrets.

Preserve the correct `TELEGRAM_CHANNEL_ID` and `TELEGRAM_BOT_USERNAME`. Mount the Firebase Admin service-account file privately and set `FIREBASE_CREDENTIALS_FILE` if app push is part of the launch. Never put it in Android source. Android `google-services.json` is client configuration, not an Admin credential.

The blueprint uses `https://www.kpn.com.ng`; confirm ownership/routing of this canonical domain. Configure Render's custom domain and DNS/TLS. Set any additional approved hosts/origins explicitly; do not use wildcard host access. Review proxy/HTTPS behaviour on the actual service.

## 3. Render commands
- Runtime: Python 3.11.15
- Build: `bash scripts/render-build.sh`
- Start: `bash scripts/render-start.sh`
- Health check: `/health/ready/`

`render.yaml` is supplied for a new/staging Blueprint. It does not automatically upgrade an existing service: verify that service's commands and environment match. It provisions no new database. Existing services still need private values and domain configuration.

The build installs `requirements.lock.txt`, validates production configuration and collects static assets. Startup obtains a PostgreSQL advisory lock, runs seat-integrity preflight, applies migrations and rechecks before starting Gunicorn. This works without a paid pre-deploy hook. Do not simultaneously enable an unrelated migration command/release process.

A duplicate verified leadership seat, mismatched tier or missing required scope blocks migration. **Resolve with an authorised administrator after backup; do not delete accounts to make deployment pass.** New migration `staff/0014` adds archive fields and verified-seat constraints.

For a reviewed paid pre-deploy workflow, `scripts/render-predeploy.sh` is available; maintain one intentional migration path. `Procfile` uses the same startup script.

## 4. Validate before routing traffic
Run on staging with its private production configuration:
```bash
python manage.py check --deploy
python manage.py check_release --database
python manage.py makemigrations --check --dry-run
python manage.py migrate --plan
```
Run `scripts/test-backend.sh` against a **separate test database**. Its test settings use a test hasher/email/storage and must never serve the live site. PostgreSQL user needs permission to create the disposable test database. CI uses isolated PostgreSQL 15.

Complete `RELEASE_ACCEPTANCE.md`; deploy the backend before the matching app. Verify old installed app versions against the upgraded APIs. No production seed or automatic superuser is supplied.

## 5. Observe and recover
Monitor readiness, HTTP 5xx, error logs, email failures, Telegram verification, upload failures, authentication anomalies and mutation conflicts. Logs must not contain credentials or sensitive request bodies. Establish alerts and an owner; a health endpoint alone is not monitoring.

Keep the pre-deploy database recovery point until the acceptance window closes. For failure, stop affected writes first, assess schema/backward compatibility, and restore only under an approved incident plan. Do not blindly reverse archive migrations or reintroduce unsafe HTML/secret fallbacks. Restoring an old backup loses subsequent writes; reconcile them before reopening service.

## 6. Signing
Follow `Kpn_android/REBUILD.md`. Use the existing release key locally or GitHub Environment secrets; do not send passwords in chat. Signing does not substitute for provider/device acceptance.

### Isolated native acceptance backend
The debug-only network security override permits HTTP only to Android emulator host bridge `10.0.2.2`, for the source's isolated instrumentation fixture. Release transport remains HTTPS-only; do not ship a debug APK or use production identities in the fixture. The full-shell test requires a local backend with the explicitly synthetic `isolated_native_president` account; the test is not self-seeding and must not target production.
