# Staging setup and verification

1. In the existing Render service, verify repository `sw7425/Staging` and branch `main`. Do not modify the production service.
2. Set the build/start/health commands shown in the root README. Keep production-shaped security checks.
3. Privately configure fresh staging settings listed in `.env.staging.example`. Generate a unique SESSION_SECRET; use a separate empty PostgreSQL database with TLS. Never paste secrets into chat or commit a populated environment file.
4. Provision separate staging provider resources only with owner approval, including any associated charges. No provider resources are provisioned by this source commit.
5. Confirm outbound recipient isolation before triggering emails, contact submissions, Telegram or push. Known contact-email recipient and Android production defaults remain explicit acceptance gates.
6. Deploy and check build logs without exposing secrets. Verify readiness, migrations, signup, approval, login/refresh/logout, role boundaries, reporting/escalation and upload behavior using synthetic test users only.
7. Record exact commit, provider delivery receipts, real Android/browser results, backup/restore and rollback evidence before any public-release decision.

GitHub Actions quality jobs use synthetic CI credentials only. They do not access the production database or certify real provider delivery. Standard hosted runners are requested; no larger runners or paid resources are configured.
