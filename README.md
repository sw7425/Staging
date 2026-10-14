# KPN isolated staging

Staging-only source repository for https://kpn-staging.onrender.com. This is a tested release candidate, **not a certified public release**.

## Existing Render service

Connect this repository, branch `main`, to the existing **kpn-staging** service. Do not connect it to the production service.

- Runtime: Python 3.11.15
- Build: `bash scripts/render-staging-build.sh`
- Start: `bash scripts/render-start.sh`
- Health: `/health/ready/`
- Set private environment values from `.env.staging.example` in Render, not GitHub source.

`render.yaml` is a reference for staging. Committing it does not automatically reconfigure a service previously created in the dashboard. Existing auto-deploy settings may trigger a build on every main-branch update.

Use a separate empty PostgreSQL database, Cloudinary account/cloud, email provider credentials and controlled test recipients, Telegram bot/channel and (when testing push) Firebase project. Never copy production secrets or member data. Production-shaped validation intentionally refuses incomplete provider configuration.

**Outbound safety gate:** the contact API currently contains the production contact recipient. Do not test contact email or any external-provider workflow until all recipients/routing are isolated and approved. Separate credentials alone do not isolate recipients. Do not register production Telegram webhooks here.

## Evidence and limitations

The source candidate previously passed 92 local backend tests on isolated PostgreSQL 15 with Python 3.11.15 and Django 5.2.17, with no migration drift. Deployment-only configuration in this repository still needs live staging validation. CI results are separate from those local results.

Uploaded media/member photos, database files, populated environments, private keys, APKs and unrelated attachments are omitted. The `media/` Django application is retained. Public runtime logos, fonts, Android resources and the Gradle wrapper are preserved.

The Android source retains its existing application ID, production defaults and public Firebase client configuration. **Do not distribute/install it as a staging build:** a separate staging app identity, URL, Firebase configuration and real-device acceptance are still required. The manual signing workflow must not be run for staging.

Public release remains blocked pending isolated provider delivery tests, real browser/role acceptance, backup restore/rollback evidence, Android device acceptance, signing/version verification and owner approval.

See `docs/STAGING_SETUP.md` and `docs/PUBLISHED_FILES.sha256`.
