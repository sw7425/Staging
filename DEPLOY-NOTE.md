# KPN FINAL PACKAGE — Deploy Note

**Package:** `KPN-FINAL-intelligence-and-page-upgrade.zip`
**From:** live production **v2.9.2** → this combined build
**Deploy:** push the contents of `kconnect-main/` to GitHub as usual. Render
builds and starts the service; `release_migrate` does everything else
automatically (see §2). No manual commands are needed at any point.

This package contains **two programmes plus a consistency completion
round in one push**, per the owner's single-push decision:

1. **KPN Statewide News Intelligence & Civic Monitoring System** (Phases
   1–7): the `intelligence` app — geographic directory (21 LGAs), collection
   engine on a ~30-minute cycle, relevance/categorisation/dedupe/clustering,
   human-gated review desk, public project tracker + promise detection,
   security desk, alerts & briefings, public LGA news pages, field
   reporting, and the premium Promise Tracker redesign.
2. **KPN Website Page-by-Page Upgrade** (Priorities 1–6): restructured
   navigation; rebuilt Home, Newsroom and Opportunities Hub (verification
   states, detail pages, expired archive, suspicious-listing reports);
   Community Hub with the full initiative lifecycle; the merged Civic
   Engagement & Advocacy section (Civic Education, Governance &
   Accountability, Community Voice, Advocacy Campaigns, Election
   Observation); Human Impact Files and Future Signal upgrades; Story
   Archive filters; Impact & Metrics with a real Kebbi coverage map and
   per-metric definitions; Code of Conduct reorganisation. Every upgraded
   page carries a real Kebbi photograph as its hero.
3. **Consistency Completion Round** (Rounds A–E, §4): every remaining
   page of the site — public, member-access and staff dashboards —
   brought to the same premium design language, so no page is left in
   the old style.
4. **ICT Admin Console** (§7): a dedicated console page at
   `/ict-admin/` for the ICT Admin holder (initially the Director of
   Media & Communications account), with super-admin accounts made
   invisible in the Contact & Mobilisation and President member lists.

**Tests:** full suite on the exact packaged tree — **1062 tests,
failures=1, errors=1, skipped=3**. The failure and error are the two
pre-existing baseline issues (a date-expired editorial test and an
Android-parity test needing the separate Android repo — see checklist
item 6). **Zero new failures** from either programme, the release
wiring, the consistency round, the ICT Admin console, the mobile
maps correction (§8), the role-name alignment (§9), the Promise
Tracker images (§10), or the tracker/LGA photo heroes (§11) in this
package.

## 1. Migrations — exactly 16 new, all additive

All 70 migration files already live in v2.9.2 are byte-identical in this
package; nothing was edited, renamed, or removed. The 16 new migrations:

- `intelligence/0001_initial` … `intelligence/0009_phase7_lga_pages_field_reports`
  (9 files — the new app, incl. `0002` seeding the 22 categories)
- `editorial/0003_source_monitoring_fields`
- `story_intelligence/0004_promise_sector`
- `story_intelligence/0005_storyprofile_impact_category_and_more`
- `story_intelligence/0006_promise_image` — one `AddField` only
  (`Promise.image`, nullable); no existing column is touched (§10).
- `core/0012_opportunity_lga_opportunity_location_text_and_more`
- `core/0013_advocacycampaign_affected_communities_and_more`
- `core/0014_ict_admin_assignment` — two `CreateModel` operations only
  (the ICT Admin assignment record and its change-history audit); no
  existing table is touched.

No `RemoveField`/`RenameField`/`DeleteModel`/`RunSQL` anywhere; every
`AlterField` only extends a choices list. **The Consistency Completion
Round (Rounds A–E, §4) added no migrations at all** — re-verified at
final packaging: exactly the same 14 new files since v2.9.2, every
shared migration byte-identical (SHA-256), and `makemigrations --check`
clean on the packaged tree. A full one-jump rehearsal from an
exact v2.9.2-state database (built and seeded with the real v2.9.2 code)
applied all 14 cleanly with **every seeded row preserved**, a no-op second
run, and a clean `makemigrations --check`. Full report:
`19-Final-Migration-Safety.md` / `12-Final-Migration-Safety.md` in the goal
workspaces. The ICT Admin console (§7) adds exactly one further migration
(`core/0014_ict_admin_assignment`, above): the first 14 files remain
byte-identical to the rehearsed set, the new migration creates two new
tables and alters nothing existing, and the full test suite builds its
database from scratch through all 16 migrations. The Promise Tracker
images (§10) add exactly one further migration
(`story_intelligence/0006_promise_image`, above): a single nullable
`AddField` on the existing promise table — existing promises simply
have no image until a member of staff uploads one.

## 2. What happens automatically on deploy

`scripts/render-start.sh` runs `python manage.py release_migrate`, which —
inside the PostgreSQL advisory lock — now does, in order:

1. `check_release` (database preflight)
2. `migrate` (applies the 16 migrations above)
3. `check_release` (postflight)
4. `backfill_story_ids` (existing; a failure here still fails the release,
   by design)
5. `seed_intelligence_directory` — fail-safe: 21 LGAs, zones, starter aliases
6. **`seed_intelligence_sources` — NEW, fail-safe:** creates the 9 starter
   intelligence sources. **Kebbi State Government Pressroom starts
   ENABLED** (the one audit-verified source); the other 8 (NAN, Premium
   Times, Daily Trust, Punch, Vanguard, Channels, Leadership, INEC News)
   start **DISABLED** pending feed validation. Idempotent and
   edit-preserving: re-runs never duplicate a row or overwrite dashboard
   edits.
7. **`backfill_article_locations` — NEW, fail-safe:** creates explicit
   article↔LGA links for existing articles (from each article's stored LGA
   field and from desk conversions), which the public LGA news pages read.
   Idempotent; later starts only link newer articles.
8. **`seed_ict_admin` — NEW, fail-safe:** records the Director of Media
   & Communications account as the ICT Admin holder if no holder is
   assigned yet. Never overwrites an existing assignment; if no such
   account exists at first start, the console stays superuser-only
   until the Super Admin assigns a holder in the Django admin panel.
9. `check_story_id_coverage` (advisory report, never blocks)

"Fail-safe" = an error in that step is logged loudly, the release
continues, and the step retries on the next start. Serving the site is
never blocked by reference-data seeding or the location backfill.

## 3. Scheduler workflows (ship in this package, inert until enabled)

Following the existing `kpn-editorial.yml` pattern (same DATABASE_URL /
Cloudinary secrets, ephemeral session secret, `migrate --check` guard):

- **`.github/workflows/kpn-intelligence.yml`** — runs
  `python manage.py run_intelligence --trigger SCHEDULED` every 30 minutes
  (cron `*/30 * * * *` UTC; Lagos is UTC+1 year-round, so cycles also land
  on the hour and half-hour in Lagos time). Scheduled runs only happen
  when the repository variable **`KPN_INTELLIGENCE_SCHEDULE_ENABLED`** is
  set to `true`; manual `workflow_dispatch` runs always work.
- **`.github/workflows/kpn-briefings.yml`** — runs
  `python manage.py generate_briefing --period daily --telegram` daily at
  22:30 Africa/Lagos (21:30 UTC) and `--period weekly --telegram` on
  Sundays at 22:30 Lagos. Gated the same way by
  **`KPN_BRIEFINGS_SCHEDULE_ENABLED`**. Telegram delivery uses the
  `TELEGRAM_BOT_TOKEN` secret and the chat IDs configured in the alert
  rules (see checklist); email uses the Lockally vars/secrets if set.
  Unset channels record SKIPPED/FAILED on the briefing — they never block
  generation.

Nothing is enabled by this package alone: after pushing, set the two
repository variables to `true` when ready (GitHub → repo → Settings →
Secrets and variables → Actions → Variables), exactly like
`KPN_EDITORIAL_SCHEDULE_ENABLED`.

## 4. Consistency Completion Round (Rounds A–E)

After Priorities 1–6 were approved, the owner directed that **every**
remaining page be brought to the same premium design language — no page
left out. Five rounds, all presentation-only (no content, permission,
route or workflow changes anywhere; **zero new migrations**):

- **Round A — hero credits removed + archive hero.** The on-page
  photograph credit line was removed from every hero across the site
  (15 templates; the mechanism and its CSS are gone entirely). The
  expired-opportunities archive page, which previously had no hero,
  gained the same photographic hero treatment (Kanta Museum courtyard,
  `static/img/opportunities-archive-hero.jpg`) — with no credit line,
  like all heroes now. Data attributions that are not photo credits
  (e.g. the OCHA/OSGOF boundary attribution on the coverage maps) are
  unchanged.
- **Round B — all remaining public pages.** About, Our Patrons,
  Leadership Directory, the public member profile, Media Gallery,
  Contact, Support KPN, FAQ and Network Rankings upgraded to the design
  language (brand tokens, photographic heroes where a real Kebbi photo
  fits — new `about-hero.jpg` and `leadership-hero.jpg` — breadcrumbs,
  premium cards/forms, mobile-first). **New 404 and 500 error pages
  were created** — neither existed anywhere in the tree before; the
  500 page is deliberately standalone so it renders even when the site
  itself is broken. The 403 page was restyled to match. Login and Join
  KPN already used the design system and were verified untouched.
- **Rounds C+D — member-access pages and dashboards.** Account pages
  (profile, notifications, change password), the member dashboard, the
  editorial desk, the story desks, Manage Opportunities, community
  report review, events, finance and the remaining staff pages were
  upgraded, and dashboards gained **real-data visuals** where the
  responsible role calls for them: server-rendered SVG charts
  (bars/donuts, no JavaScript chart libraries) and the real Kebbi
  geographic map (validated HDX geometry) — e.g. the President's
  dashboard shows member counts with the coverage map, a member sees
  their own LGA highlighted on the map, the editorial desk charts its
  own item workflow. Every visual aggregates **only records that role
  already accesses**; each role keeps its own dashboard, content,
  links and permissions exactly. One judgement call to note: the
  Treasurer dashboard's charts aggregate the **full donation register**
  (monthly totals across all statuses, and status counts including
  RECORDED) although the page's lists show UNVERIFIED + VERIFIED —
  aggregates only, no donor names or individual records are exposed,
  and the Treasurer is the register's owning role.
- **Round E — role dashboards final polish.** All **42 role dashboard
  templates** (`staff/dashboards/` — state executives, zonal, LGA and
  ward officers, general member) plus their 3 shared includes were
  polished to the identical finish (stat cards, step chips, icon
  system, tables, empty states), with charts added only where the
  dashboard's existing context already carried the aggregates (Vice
  President zone bars; LGA/Ward Coordinator report-status bars).
- **Post-round fix (owner review, 2026-11-24) — two cards branded.**
  Two cards still rendered plain white and were brought to the same
  branded hero treatment (the brand-green gradient of `.kpn-page-hero`),
  presentation only — no text, content, link or behaviour changed:
  (1) the **dashboard workspace header card** (the shared
  `staff/dashboards/includes/_role_header.html` include used by the
  member dashboard and all role dashboards — its typing welcome text
  and "Your KPN workspace" wording are untouched; the fix is CSS-only
  in `static/staff/css/kpn-design.css`), and (2) the **Telegram connect
  page hero band** ("Connect Your Telegram Account" in
  `telegram_integration/templates/telegram_integration/connect.html`;
  the band's header markup was also repaired — it was malformed, which
  is why the band rendered white). Full suite after the fix: **974
  tests, zero new failures**; no migrations. Report:
  `19-Consistency-Fix-Dashboard-Telegram-Report.md` in the page-upgrade
  goal workspace, with screenshots in
  `files/screenshots/consistency-fix-telegram-dashboard/`.

Per-round reports and full screenshot sets are in the page-upgrade
goal workspace (`14-` to `17-Consistency-*.md`).

## 5. Post-deploy owner checklist

1. **Alert-rule recipients:** in `/editorial/intelligence/alerts/rules/`,
   set the email addresses and Telegram staff chat IDs per rule —
   including the **BRIEFING** rule row, which the short Telegram
   briefings are delivered to. Until set, those channels record SKIPPED
   (by design; nothing is sent anywhere by default).
2. **Starter sources review:** in the intelligence source directory,
   review the 8 disabled starter feeds (NAN, Premium Times, Daily Trust,
   Punch, Vanguard, Channels, Leadership, INEC News) and enable each one
   only after its feed validates. The Pressroom is already enabled.
3. **Cloudinary one-time verification:** upload a throwaway image to the
   live Cloudinary account, run the story redaction flow on it, and
   confirm the object is actually destroyed/restricted. Redaction has
   only ever been proven against a test double.
4. **Impact & Metrics figures (D-01):** sanity-check the page's computed
   figures (members, active LGAs/wards, coverage map) against KPN's
   organisational records. The page computes everything live from the
   database and invents nothing — if a figure looks wrong, the records
   are what need fixing, not the page.
5. **Code of Conduct PDF (D-15):** when the official document is ready,
   supply the PDF so the download button can be wired in. No placeholder
   ships in this package.
6. **Known test-only issue:** the suite has one pre-existing failure — an
   editorial brain-API test with a hardcoded deadline of 2026-11-03 that
   has now passed (plus one pre-existing Android-parity error that needs
   the separate Android repo). Both predate this build, are test-only,
   and do not affect the site in any way.
7. **ICT Admin holder:** the ICT holder starts as the Director of Media
   account (seeded automatically on first start — see §2 step 8) and
   can only be changed by the Super Admin in the Django admin panel
   (Core → ICT admin assignment). The console page only displays who
   holds it; there is no way to change it from the website itself.

## 6. Rollback

The migrations are additive-only, so rolling back is: redeploy the
previous (v2.9.2) code. New tables/columns are simply ignored by the old
code; no data migration needs reversing. The two scheduler workflows can
be switched off instantly by setting their repository variables back to
anything other than `true`.

## 7. ICT Admin Console (owner instruction, 2026-11-24)

A dedicated console for KPN's ICT Admin — there is no separate ICT Admin
role; the holder is whoever the assignment record names.

- **Page:** `/ict-admin/`, linked as "ICT Admin" under the Editorial
  group in the navigation (desktop and mobile), rendered only for the
  holder or a superuser. It shows a system overview (members, verified
  members, pending verification, position holders, published articles,
  open opportunities, pending community and suspicious-listing reports),
  member-system panels with server-rendered charts, every leadership
  office with its current holder, editorial and intelligence summaries
  (items by state, source health, latest collection runs, alert
  failures), content and Telegram panels, and quick links into the
  existing management pages. All figures are computed live from real
  records; superuser accounts are excluded from every member figure and
  list on the page.
- **Access:** the assigned holder or any superuser. Everyone else —
  including the President, unless he is the holder or a superuser — is
  redirected to their dashboard with the site's standard access message.
- **Assignment:** stored in a single-row record
  (`core.IctAdminAssignment`, migration `core/0014`). The ICT holder
  starts as the Director of Media account and can only be changed by
  the Super Admin in the Django admin panel — the Super Admin works
  only through the Django admin panel, so the console itself carries no
  change controls, only a display of the current holder and the change
  history. Super admin accounts can never be assigned as the holder.
  Every change made in the admin panel writes an audit row
  (`core.IctAdminChange`: previous holder, new holder, changed by,
  when).
- **Super Admin invisibility:** superuser accounts never appear in the
  Contact & Mobilisation member list or in the President's
  member-management surfaces (Manage Members list, member approvals,
  the President dashboard's pending list, member CSV/PDF exports and
  the position-swap picker), and the President's member action routes
  (edit role, promote, demote, dismiss, suspend, reinstate) return 404
  for a superuser account exactly as for an unknown member. Nothing
  else about those pages changed.

## 8. Mobile Maps Correction (owner instruction, 2026-11-24)

The owner reversed the earlier desktop-only instruction for the Kebbi
geographic map: **the map now renders at every screen width**, on
phones as well as desktop. This section supersedes any desktop-only
wording about the maps elsewhere in this note.

- Every surface that shows the real Kebbi map — the intelligence
  tracking map (`/editorial/intelligence/map/`), the map embedded in
  the intelligence overview, the public Impact & Metrics coverage map
  (`/impact/`), the President dashboard's coverage map, and the member
  dashboard's own-LGA map — now displays the map inside its card at
  full phone width, with enlarged LGA labels so the names stay
  legible at 390px. On mobile the selected-LGA detail panel and the
  LGA list stack **below** the map (the list complements the map; it
  no longer replaces it). Desktop layout is unchanged, tap/click
  selection works at both widths, and the COD-AB/OCHA boundary
  attribution remains on the public map.
- Presentation only: the per-LGA figures, choropleth shading and
  selection logic are the same live computed values as before — no
  view, queryset or model changed, and **no migration was added**
  (the count since v2.9.2 stayed at exactly 15 at that point; §10
  later brought it to 16).

## 9. Role-Name Alignment (owner instruction, 2026-11-24)

Every user-facing surface now displays the **canonical constitutional
role names** from `leadership/roles.py` (e.g. LGA Coordinator →
**LGA Network Lead**, Zonal Coordinator → **Senatorial Director**,
Treasurer → **Director of Finance**, Ward Coordinator → **Ward
Community Lead**, LGA Secretary → **LGA Administrative Officer**).

- All role dashboards, page titles, responsibility text, the donations
  pages, member/staff lists, profiles, the public leadership page, the
  ICT Admin roles panel, role dropdowns and the approval email now
  render canonical titles. Titles rendered from a stored
  `RoleDefinition` pass through `canonical_role_title()` at render
  time (a `RoleDefinition.canonical_title` property), so even a member
  whose stored title is a legacy alias displays the new name — and the
  dashboard dispatcher now routes such members to their role dashboard
  instead of the generic one. **No stored data was changed.**
- The dashboard typing/welcome phrases were aligned too: no phrase
  names an old role, and the one phrase that names a role now uses the
  canonical title ("Supporting the Ward Finance Officer."). The
  typing mechanism, timing and styling are unchanged.
- Display layer only: template filenames, dashboard URL names,
  `DASHBOARD_BY_ROLE` keys, model fields and permission identifiers
  are unchanged, and **no migration was added** (the count since
  v2.9.2 stayed at exactly 15 at that point; §10 later brought it
  to 16).

## 10. Promise Tracker Images (owner instruction, 2026-11-24)

A promise can now carry **its own image** on the public Promise
Tracker, uploaded by staff before the promise appears there.

- **Model:** `Promise.image` — an optional `ImageField`
  (`upload_to='promises/'`), using the same storage as article
  featured images (Cloudinary in production). Migration
  `story_intelligence/0006_promise_image` is a single nullable
  `AddField`; existing promises are untouched and keep working with
  no image. **The migration count since v2.9.2 is now exactly 16.**
- **Staff side:** the promise desk create/edit form (`PromiseForm`)
  carries the image upload, with a preview of the current image on
  the edit page and the usual clear/replace control. No other form
  behaviour changed. Note: promises confirmed from the intelligence
  candidate queue are created by that queue's own confirm flow,
  which does not carry the field — staff can add or change the image
  immediately afterwards on the promise's desk edit page, which is
  where the confirmed promise lands.
- **Public display:** tracker cards use the promise's own image
  first; if none, the previous behaviour is unchanged — the featured
  image of the first linked published story, otherwise the
  sector-icon tile. The promise detail page shows the image
  prominently below the hero (alt text = the promise title) with
  the same fallback order, and renders exactly as before when no
  image exists anywhere. No image was fabricated or seeded onto any
  real record; sample images exist only in the throwaway screenshot
  database.

## 11. Photo Heroes for the Tracker and LGA Pages (owner instruction, 2026-11-24)

The Promise Tracker, Project Tracker and public LGA News pages were
built with plain brand-green gradient heroes before the standing rule
that every public page carries a real Kebbi photographic hero; they
were the last public pages without one. They now carry the same
photographic hero treatment as the rest of the site (the
`.kpn-page-hero-photo` pattern: photograph under the brand-green
gradient overlay, **no credit line**):

- **Promise Tracker** (list + detail): `static/img/promises-hero.jpg`
  — an aerial photograph of the Kebbi State Secretariat complex,
  Birnin Kebbi (via the Kebbi State Government website), 1280×436.
- **Project Tracker** (list + detail): `static/img/projects-hero.jpg`
  — an aerial photograph of the Sokoto–Badagry Super Highway under
  construction on the Kebbi corridor (via Hitech Construction
  Company), 1920×1080.
- **LGA News** (index + every LGA detail page, one shared image):
  `static/img/lga-hero.jpg` — an aerial photograph of the Argungu
  Jumat Mosque and the surrounding town (via the Lababidi
  Foundation), 1080×620. It is a different scene from the Argungu
  festival photographs used on the Home and Newsroom pages.

All three are real photographs of Kebbi State, visually checked
before wiring; none is a generated image, none duplicates another
page's hero, and none is dominated by a politician's portrait or
party branding. Hero text, statistics, search and page layout are
unchanged — only the hero gained the photograph. Presentation only:
**no migration was added** (the count since v2.9.2 stays at exactly
16), and `makemigrations --check` is clean. Seven new tests pin the
photo hero and the absence of any credit line on all six pages.

## 12. API Completion for the Native App (2026-11-24)

Ahead of the native app rebuild, the public REST API (`/api/v1/`) was
completed so that every public website surface the app needs is now
served. All changes are additive — no existing route, field or
response shape was removed or changed:

- **15 new routes** in `rest_api/urls.py` (new read views in
  `rest_api/public_content.py`): the API root and a public health
  endpoint (`GET /api/v1/health/`), Promise Tracker list + detail,
  Project Tracker list + detail, Future Signal list + detail, Human
  Impact Files list + detail, the Story Archive, LGA news index +
  detail, and the expired-opportunities archive.
- **Article detail** gained the `story_file` block — the Story ID,
  claims, updates, evidence and corrections the article page's Story
  File panel shows.
- **Serializers brought current:** the opportunity and community
  serializers match the current models, and an opportunity in
  VERIFICATION_PENDING withholds its application link, exactly as
  the website does.
- **Impact endpoint** gained the metric groups and the LGA coverage
  data behind the Impact & Metrics page.
- **Version and schema:** the API now reports version **1.1.0**, and
  the OpenAPI schema generates with **0 errors**. **No new
  migrations** — the count since v2.9.2 stays at exactly 16. Full
  report: `25-API-Completion-Report.md`; the endpoint inventory for
  the app rebuild: `26-API-Endpoint-Inventory-for-App.md` (both in
  the page-upgrade goal workspace).
- **Post-deploy check:** after deploying, open
  https://kpn-staging.onrender.com/api/v1/health/ and confirm it returns status
  ok with api_version 1.1.0 before starting the app rebuild.

## 13. Staff API for the Android 2.0.0 Rebuild (2026-11-25)

The staff screens of the native app (screens 160–202, plus five
Rule-3 gaps in screens 1–159) are now served by the REST API. All
changes are additive — new views in `rest_api/staff_views.py`, new
routes in `rest_api/urls.py`, no existing route, field or response
shape removed or changed, and **no new migrations**
(`makemigrations --check` is clean; the count since v2.9.2 stays at
exactly 16). Every write goes through the same service layer or form
the web desk uses, so state machines, capability checks and audit
rows are identical on web and mobile; state names are the model
choice values in `intelligence/models.py`, unchanged.

New endpoint groups (all under `/api/v1/`):

- **Rule-3 gaps:** `POST opportunities/<slug>/report/` (public,
  throttled 5/hour per IP like the web route, creates the same
  `OpportunityReport` record), `field-reports/` GET + POST,
  `GET impact/rankings/` (the web Network Rankings data),
  `GET events/calendar/`, `GET events/minutes/`,
  `POST advocacy/<id>/decision/` (see the flag in
  `28-Staff-API-Endpoints-Report.md`: the web has no advocacy
  decision workflow, so this is a minimal gated status transition
  recorded as an `AdvocacyActivity` entry).
- **Intelligence (screens 160–188):** `intelligence/overview/`,
  `items/`, `desk/` + `desk/<ref>/` + `desk/<ref>/transition/` +
  `desk/<ref>/convert/` (units are addressed as `cluster-<pk>` /
  `item-<pk>`), `clusters/` + detail + `merge/` + `split/`,
  `sources/` (GET + POST), `runs/`, `aliases/` + `aliases/<id>/review/`,
  `lgas/` + `lgas/<slug>/`, `article-locations/` (GET + POST),
  `briefings/` + detail + `briefings/generate/` +
  `briefings/<id>/telegram/`, `alert-rules/` + `alert-rules/<id>/`
  (GET/PUT), `alerts/` + `alerts/<id>/read/`, `promise-candidates/` +
  detail + `confirm/` + `dismiss/`, `security/` + `security/<ref>/` +
  `security/<ref>/transition/` + `security/incidents/` + detail +
  `claim/` + `status/` + `redact/`, `projects/<id>/` + `status/` +
  `milestone/` + `evidence/`, and `map/`.
- **Story intelligence (screens 189–194):**
  `story-intelligence/promises/desk/` + `promises/<id>/desk/`
  (GET + PATCH) + `promises/` POST + `promises/<id>/updates/` +
  `promises/<id>/status/`, `stories/<id>/desk/` (GET + POST actions),
  `radar/`, and `redact/` (POST, reusing `perform_redaction`
  unchanged — Cloudinary removal is verified by the service before
  anything is written).
- **Editorial (screens 195–197):** `editorial/`, `editorial/<id>/`,
  `editorial/sources/`, `editorial/sources/<id>/approve/` (see the
  flag in the report: the web has no separate approve action; this
  performs the web's enable-on-save with the same manager gate and
  SOURCE_SAVED audit).
- **ICT Admin console (screens 198–202):** `ict/overview/`,
  `ict/members/`, `ict/leadership/`, `ict/intelligence/`,
  `ict/telegram/` — read-only (POST returns 405), gated exactly like
  the web console: the assigned ICT Admin holder or a superuser;
  everyone else receives 403.

**Gating:** intelligence endpoints use the web views' capability
semantics (`leadership/capabilities.py`, superuser included);
story-intelligence endpoints use the web story desk's exact rule
(capability only); the security desk remains holders-only
(Director of Media & Communications and President — decision D6);
editorial uses `editorial/access.py`; ICT uses
`core.ict_admin.is_ict_admin`.

**Tests:** 29 new tests in `rest_api/tests/test_staff_api.py`. Full
suite after `collectstatic`: **1091 tests, failures=1, errors=1,
skipped=3** — identical to the pre-existing baseline (1062 tests,
failures=1, errors=1, skipped=3); zero new failures. The OpenAPI
schema generates with **0 errors** and contains every new path.
Full inventory and flags: `28-Staff-API-Endpoints-Report.md`.

**Post-deploy note:** the mobile staff tools (Intelligence,
Editorial and ICT screens) require these endpoints deployed — after
deploying, confirm `GET https://kpn-staging.onrender.com/api/v1/schema/` lists the
`intelligence/`, `story-intelligence/`, `editorial/` and `ict/`
paths before enabling the staff tools in the app.

## 14. Round 2 — Completion of the 25 PARTLY BUILT App Screens (2026-11-25)

Server-side completion for the Android 2.0.0 rebuild's 25 PARTLY
BUILT screens. All changes are additive — no existing route, field or
response shape was removed or changed. Full detail:
`website-round2-notes.md` in the Android rebuild workspace.

**One new migration — additive only.** `donations/0006_donation_rejection`:
three `AddField`s on `Donation` (`rejected_by`, `rejected_at`,
`rejection_reason`, all nullable/blank) plus one `AlterField` that
only extends the status choices with `REJECTED`. No existing column
is touched; the migration count since v2.9.2 is now exactly **17**.
It was unavoidable: the Donation model had no rejected status and
nowhere to store a rejection reason, and a reject transition without
either is not a real record. `release_migrate` applies it
automatically like every other migration; rollback is unchanged
(redeploy the previous code — the new columns are simply ignored).

What round 2 adds:

- **Payload enrichments:** community-reports carry `lga_name` /
  `ward_name` (screen 43); the advocacy detail payload mirrors the
  web detail page — `objective`, `proposed_action`,
  `affected_communities`, `responsible_team`, `status` +
  `status_label`, and `evidence_articles` (PUBLISHED only, each with
  its Story ID) (45); the leadership directory carries `tier` (115);
  the ICT telegram payload carries `bot_username` and `channel_id`
  (the identity the site actually holds in settings — no channel
  handle is stored anywhere, so none is returned) plus server-side
  `leaders_total` / `leaders_linked` / `leaders_not_linked` counts
  (202).
- **New endpoints:** `POST finance/donations/<id>/reject/` — reason
  required, gated on VERIFY_DONATION exactly like verify, UNVERIFIED
  → REJECTED with actor/timestamp/reason recorded (93);
  `POST intelligence/runs/trigger/` — one manual collection cycle
  through `intelligence/cycle.py` with trigger MANUAL under the
  existing lease machinery, gated on MANAGE_SOURCES, producing a
  normal CollectionRun record (170); `POST intelligence/aliases/` —
  alias create through the web's GeoAliasForm, PENDING under the
  existing review workflow, ALIAS_CREATED audit, MANAGE_SOURCES (171).
  The intelligence overview web page gains a small "Run now" button
  beside Pause/Resume under the same gate, posting to the same cycle
  service.
- **Contract completions:** the member decision endpoint also
  accepts the note as `note` (stored by the shared
  `decide_application` service as `archive_reason` on rejection,
  exactly like the web flow's `reason`) (70); report create accepts
  optional `report_type` / `submitted_to`, validated against the
  values the workflow derives from the submitter (73).
- **Verified already-correct, now locked by tests:** community-report
  create returns the created `id` (48 — the model has no
  reference/code field and none was invented); management endpoints
  accept JSON and multipart (100); the article decision literals are
  `publish` / `return` / `reject` in the `action` field (133/134);
  field-report create accepts `occurred_at` and a multipart photo
  (138); project evidence accepts a multipart file and JSON URL
  evidence (187); staff promise create and desk edit accept the
  image as multipart (191); redaction accepts a Story ID
  (`KPN-SI-YYYY-NNNNN`) via `story_id` or as `target_id`, as well as
  the numeric id (194).
- **Flagged, deliberately unchanged:** FAQ category (53) — the FAQ
  model has no category field and the web FAQ page's categories are
  hard-coded template markup over hard-coded questions, so there is
  no data source to mirror; announcement read flag (81) — no
  read-tracking exists for announcements; legal-opinion draft (118)
  — the web legal review has no draft concept; event audience (123)
  — the Event model has no audience field; alert-rule create (177)
  — the reference screen does show a "New rule" label, but rule kinds
  are a fixed server-side set, so this is flagged for the owner and
  nothing was built; incident form (184) — the API already uses the
  exact web SecurityIncidentForm, and reported figures belong to the
  claim step (screen 185), so there is no mismatch to fix.

**Tests:** 34 new tests in `rest_api/tests/test_round2_api.py`, all
passing. Full suite after `collectstatic`: **1125 tests,
failures=1, errors=1, skipped=3** — identical to the pre-existing
baseline (1091 tests, failures=1, errors=1, skipped=3); zero new
failures. The OpenAPI schema generates with **0 errors** and
contains every new path.

## 15. Round 3 — Missing Screens and the Five Remaining Partials (2026-11-25)

Closes the gap audit (`Web-Screens-Missing-From-App.md`: 226 web pages,
217 covered, 9 missing) and the five PARTLY BUILT app screens left after
round 2 (53, 81, 118, 123, 177). All changes are additive — no existing
route, field or response shape was removed or changed. The Django admin
stays web-only by owner instruction; no endpoint was built for it. Full
detail: `30-Missing-Screens-and-Partials-Report.md`.

**Two new migrations — both additive. Migration count since v2.9.2 is
now exactly 19** (17 prior + these 2). `release_migrate` applies them
automatically like every other migration; rollback is unchanged
(redeploy the previous code — the new tables/column are simply ignored).

- `staff/0017_announcementread_legalopiniondraft`: two new tables —
  `AnnouncementRead` (per-user announcement read tracking, screen 81)
  and `LegalOpinionDraft` (server-side legal-opinion drafts, screen
  118). No existing table is touched.
- `events/0004_event_audience`: one `AddField` — `Event.audience`
  (screen 123). No existing column is touched.

**Part A — new endpoints for the seven missing web pages** (in
`rest_api/staff_views.py`, routes in `rest_api/urls.py`):

- `GET/POST intelligence/security/<kind>/<pk>/working-copy/` — Security
  Desk working copy of a restricted cluster/item: prepare a redacted
  working copy and blank passages in it (holders only — Director of
  Media & Communications and President, decision D6). Closes report
  28's flag F5. App screen 203.
- `GET account/id-tag/` — the member's printable ID-tag PDF (the same
  document the web `account/generate-id-tag/` produces). App screen 204.
- `POST intelligence/projects/from-<kind>/<pk>/` — create a tracked
  project directly from a desk cluster/item. App screen 205.
- `POST editorial/sources/` (create) and
  `PUT editorial/sources/<int:pk>/` (edit) — the editorial workspace
  source form, manager-only, alongside the existing list and approve
  endpoints. App screen 206.
- `GET events/attendance/logs/` — the cross-event attendance register
  (the 50 most recent recorded attendances). App screen 207.
- `POST intelligence/lgas/<slug>/districts/` and
  `POST intelligence/lgas/<slug>/communities/` — add a district or a
  community to an LGA's geography. App screens 208/209.
- `POST story-intelligence/promises/institutions/` — add an institution
  to the Promise Desk directory. App screen 210.

**Part B — the five remaining partials, completed with real data:**

- **53 FAQ categories:** new `core/faq_categories.py` carries the FAQ
  category mapping extracted verbatim from the web FAQ template's
  hard-coded `data-category` attributes; the FAQ serializer now returns
  a `category` field driven by that mapping. No FAQ model change, no
  invented mappings.
- **81 Announcement read tracking:** the `AnnouncementRead` model
  (migration above), `POST announcements/<id>/read/`, and `is_read` in
  the announcement payloads.
- **118 Legal-opinion drafts:** the `LegalOpinionDraft` model
  (migration above) and `GET/PUT discipline/<id>/legal-opinion/draft/`;
  submitting the final opinion clears the draft.
- **123 Event audience:** `Event.audience` (migration above), carried
  in the event create/detail payloads.
- **177 Alert-rule create:** `POST intelligence/alert-rules/` creates
  the configuration for a rule kind that is not yet configured. Rule
  kinds remain the fixed server-side vocabulary (`ALERT_RULES`) — this
  does not create new kinds, it configures an existing unconfigured
  kind, edited thereafter through the existing
  `GET/PUT intelligence/alert-rules/<id>/`.

**Tests:** 39 new tests — `rest_api/tests/test_staff_api_part_a.py`
(26, Part A) and `rest_api/tests/test_part_b_data.py` (13, Part B) —
all passing. Full suite after `collectstatic`: **1164 tests,
failures=1, errors=1, skipped=3** — identical to the pre-existing
baseline (1125 tests, failures=1, errors=1, skipped=3); zero new
failures. The OpenAPI schema generates with **0 errors** and contains
every new path; `makemigrations --check` is clean.

**Post-deploy note:** app screens 203–210 and the completed screens
53/81/118/123/177 light up only after this package is deployed and the
two migrations have run.

## 16. Round 4 — ID Tag Monthly Limit and 400 KB PDF Cap (2026-11-25)

Owner instruction (2026-11-25): ID tag generation was abused in the
past (repeated generation exhausted the Render instance hours). Two
server-side rules now bind the web page and the API equally, plus an
app-side cache.

- **One generation per account per calendar month.** New additive
  model `staff.IdTagGeneration` (user FK + `generated_at`) records
  each SUCCESSFUL generation; migration
  `staff/migrations/0018_idtaggeneration.py` — purely additive, one
  CreateModel. The migration count since v2.9.2 is now exactly
  **20**. The shared service `staff/id_tags.py` reckons the calendar
  month in Africa/Lagos (site TIME_ZONE): a generation on or after
  the 1st of the month blocks any further generation until the 1st
  of the next month. The check runs BEFORE any PDF work in
  `generate_id_tag` (web) and in the API's `IdTagView` — web refusal
  is the standard error message + dashboard redirect; the API
  returns **HTTP 429** with `{"detail": "You have already generated
  your ID tag this month. You can generate a new one from 1
  <Month> <Year>."}` (the 1st of next month). A log row is written
  exactly once, only on success. There are no role exemptions (the
  pre-existing GENERAL-member block is unchanged and takes
  precedence).
- **PDF capped at 400 KB.** The composed front/back cards are now
  embedded as JPEG (quality 85) instead of PNG — no design, wording
  or layout change. Measured with a realistic member photo: **450.8
  KB before → 312.3 KB after** (cap 400 KB, target headroom met).
- **App (screen 204):** the downloaded PDF is cached per member at
  `cacheDir/idtag/<userId>/id-tag.pdf` with a download-date marker;
  Open/Share reuse the cache with no API call; an explicit "Generate
  new ID tag" action downloads again and surfaces the 429 message
  plainly.

**Tests:** 10 new tests (staff/tests/test_id_tag_limit.py,
rest_api/tests/test_id_tag_limit_api.py) — limit on both surfaces,
no log row on refusal, previous-month row does not block, GENERAL
block unchanged, PDF size <= 400 KB with a photo. Full suite:
**1174 tests, failures=1, errors=1, skipped=3** — identical to the
pre-existing baseline; zero new failures. OpenAPI schema 0 errors;
`makemigrations --check` clean.

**Post-deploy note:** the limit and cap take effect on both the web
page and the app as soon as this package is deployed and the
migration has run; no member action is needed.

## 17. Round 5 — ID Tag Size Cap Removed (owner decision, 2026-11-25)

The owner lifted the §16 **400 KB PDF cap** the same day: the app
cache lives on the member's phone and does not occupy database
storage, so the tag returns to its normal full-quality size in the
cache.

- **What changed:** only the embedding in
  `staff/views.py::generate_id_tag` — the composed front/back cards
  are embedded as **full-quality PNG** again (300 DPI metadata, same
  temp-file flow), exactly as before round 4. No design, wording or
  layout change.
- **Restored size:** measured through the web view with the round-4
  noisy-photo fixture member: **590,434 bytes (~576.6 KB)**, versus
  319,762 bytes for the compressed build under its own measurement —
  the full-quality embedding is back. (Round 4's original
  pre-compression baseline was 461,605 bytes with a different photo
  fixture; the size varies with the member's photo, as it always
  did.)
- **Unchanged:** the **monthly generation limit** (§16) — 1 per
  account per calendar month, web + API, log, HTTP 429 — and the
  app's per-member cache behaviour. **No app change and no new
  migration in this round.**
- **Tests:** the round-4 size test now asserts a with-photo
  generation succeeds at full quality (larger than the compressed
  build's 319,762 bytes, within a sane two-page band); all limit
  tests untouched. Full suite: **1174 tests, failures=1, errors=1,
  skipped=3** — identical to the pre-existing baseline; zero new
  failures.
