# KPN — How to test: STAGING first, then PRODUCTION (kpn-staging.onrender.com)

Rule: **always test on staging first. Production (kpn-staging.onrender.com) only receives code
that has passed on staging.** Nothing in these packages contains staging
accounts, staging passwords or staging data — staging test members are created
by you, on the staging site itself, through the normal Join page and the
Django admin, exactly like real members.

Packages:
- Website: `KPN-FINAL-intelligence-and-page-upgrade.zip` (site code + DEPLOY-NOTE.md)
- App: `KPN-Android-2.0.0-rebuild.zip` (Android Studio project, folder `Kpn_android`)

---

## A. STAGING — website

1. **Separate GitHub repo.** Create a staging repo in your staging GitHub
   account and put the website code there. Never mix it with the production
   repo (`Kebbiconnect/kconnect`).
2. **Separate Render service.** Log in to Render with GitHub (same as
   production), create a New Web Service from the staging repo.
3. **Separate database.** Create a NEW Neon database/project for staging.
   Never point staging at the production database.
4. **Environment variables** (same names as production, staging values):
   - `DATABASE_URL` — the staging Neon database
   - `KPN_ENV=staging`, `DEBUG=False`
   - `SESSION_SECRET` / `SECRET_KEY` — a fresh staging value
   - Cloudinary variables — use a separate Cloudinary account or folder for
     staging so test uploads never mix with production media
   - Email (Lockally) variables — staging values, or leave email unconfigured
     on staging if you are not testing email
   - `KPN_EDITORIAL_AUTO_PUBLISH` — leave OFF on staging unless you are
     deliberately testing auto-publishing
   - Telegram variables — leave unset on staging unless testing the bot;
     never use the production bot token on staging
5. **Schedulers OFF on staging.** In the staging repo's GitHub variables, do
   NOT set `KPN_INTELLIGENCE_SCHEDULE_ENABLED` or
   `KPN_BRIEFINGS_SCHEDULE_ENABLED`. The 30-minute intelligence cycle and the
   briefings must only ever run against production.
6. **Deploy.** Render builds and runs the normal release flow (migrations
   included). All migrations are additive.
7. **Create your test members on staging.** Register through the staging
   site's own Join page, then assign roles/positions in the Django admin or
   through the President account's member management, the same way production
   members are managed. Use a password you choose yourself, only on staging.
8. **Verify on staging** (report any failure before touching production):
   - `https://<staging-host>/api/v1/health/` → status ok, api_version 1.1.0
   - Home, Newsroom, Opportunities, Promise Tracker and an LGA page load
   - Sign in as Director of Media → Intelligence overview and ICT Admin open
   - An article and an opportunity open end to end
   - ID tag: first generation works; a second one in the same month is refused
     with the monthly message

## B. STAGING — app

The app points at production by default. To test it against staging you do
**not** edit code: the debug build accepts an API address override.

- In Android Studio, open the Run/Debug configuration's Gradle options (or
  `gradle.properties`) and set the property:
  `debugApiBaseUrl=https://<staging-host>/api/v1/`
- Build and run the **debug** variant on your phone (see
  `FIRST-COMPILE-GUIDE.md`). Sign in with the test members you created on
  staging and check the screens role by role.
- Remove the property (or build the release variant) and the app is back to
  production: `https://kpn-staging.onrender.com/api/v1/`.

## C. PRODUCTION — kpn-staging.onrender.com (only after staging passes)

1. Push the website code to the production GitHub repo
   (`Kebbiconnect/kconnect`). Render deploys automatically.
2. Verify immediately:
   - `https://kpn-staging.onrender.com/api/v1/health/` → status ok, api_version 1.1.0
   - Home, Newsroom, Opportunities and one article load normally
   - Sign in as Director of Media and open the Intelligence overview
3. Scheduler variables on the production repo stay as they are
   (`KPN_INTELLIGENCE_SCHEDULE_ENABLED=true`,
   `KPN_BRIEFINGS_SCHEDULE_ENABLED=true`).
4. Compile the app (release variant, default production address), run it on
   your phone, and confirm the staff screens (160–210) now connect, since
   their endpoints went live with this deploy.

If anything fails on staging, fix it there and re-test. Production is never
the place to discover a problem.
