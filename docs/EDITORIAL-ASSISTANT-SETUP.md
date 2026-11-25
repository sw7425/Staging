# KPN Editorial Assistant — v2.9.2 FINAL, Muse is the brain (auto-publish edition)

v2.9.2 (2026-11-02, template polish on v2.9.1): owner-requested
presentation pass over the KPN Media public pages — the explanatory
intro paragraphs were removed from the Promise Tracker, Story
Archive, Future Signal and Human Impact pages and from the article
Story File panel (headings, labels and real content unchanged), the
pages were restyled onto the site's own template patterns (green
header band, kpn-input/kpn-btn controls, site empty states), and the
header navigation now groups all newsroom features under one
"Newsroom" dropdown (desktop) and one "Newsroom" section in the
mobile hamburger menu, mirroring "KPN Network". NO migrations, NO
backend or behaviour changes — templates and template-content tests
only. This package supersedes v2.9.1 as the deployment candidate.

v2.9.1 (2026-11-01, deployment-automation patch on v2.9.0): NOTHING
about the site changes — this patch removes the last manual deploy
step. The Story ID backfill now runs AUTOMATICALLY on every deploy:
`release_migrate` (which scripts/render-start.sh already runs before
Gunicorn on every start, inside the same PostgreSQL advisory lock
that serialises migrations) now runs, in order: check_release
--database → migrate → check_release --database →
backfill_story_ids → check_story_id_coverage.
scripts/render-predeploy.sh mirrors the same sequence. The backfill
is the same idempotent command as before — it assigns IDs only to
published articles missing one and is a fast no-op when coverage is
complete. A backfill ERROR fails the deploy loudly on purpose (the
start script stops before Gunicorn serves traffic). The coverage
REPORT is advisory: it prints in the deploy log and always exits 0 —
the only gap possible right after a backfill is an article published
by a still-draining old instance in the deploy window, which
self-heals on the next deploy's backfill and is also caught by the
story_intelligence audit backstop, so it must never block a healthy
deploy. This patch ships ZERO migrations (all 70 migration files
byte-identical to v2.9.0), changes no publish-path behaviour, and
SUPERSEDES the manual backfill steps in the v2.5.0–v2.9.0 deploy
notes below: those commands now run by themselves — the owner never
needs a shell on Render (the free tier has none to give).

v2.9.0 (2026-11-01, KPN Media Phases 5 + 6 — About Us + editorial
practice) is the FINAL accumulated KPN Media build: Phases 1–4
(v2.5.0–v2.8.0, below) plus these two closing phases. It ships ZERO
new migrations — the migration set is byte-identical to v2.8.0.

PHASE 5 — ABOUT US: the existing About page (/about/,
core/templates/core/about.html) is preserved in full — URL,
structure and every word of the existing organisational content —
and gains ONE new section, "KPN Media: How Our Newsroom Works for
You", explaining the seven pillars (Living Stories, Public Promise
Tracker, Future Signal, Community Radar, Truth Layers, Human
Impact Files, KPN Knowledge Archive) in plain reader language,
with links to the public surfaces and an honest "How KPN checks
what it publishes" panel (an official statement is not a confirmed
fact; corrections are published openly). Template-only change; no
view, URL or model was touched. Tests:
core/tests/test_about_kpn_media.py (7 tests) pin the page at 200,
the existing content markers, all seven pillar names, and that no
internal/technical term renders on the page.

PHASE 6 — EDITORIAL PRACTICE: docs/KPN-MEDIA-EDITORIAL-GUIDE.md
(inside this package) is the team's single operating reference:
claim-status definitions (an official statement is never itself
"confirmed"), verification practice and evidence records,
corrections vs story updates, Story ID + backfill operations,
promise verification rules and next-check dates, community-report
privacy (claims from reports start NOT_ESTABLISHED; reporter
details never published), consent rules and the publish guard,
emergency redaction procedure, archive-instead-of-delete, the
capability table, and the automated brain-run rules (scheduled
publishing only, 24-hour recency, no duplicates, byline "KPN
Editorial Team", application links copied exactly from evidence).

====================================================================
COMPLETE DEPLOYMENT CHECKLIST — v2.9.1 FINAL (production is on
v2.4.4; this is the one accumulated KPN Media deployment)
====================================================================
Do these in order. Do not skip step 1. There is NO step that asks
you to run a command or open a shell — the free tier gives you
none, and since v2.9.1 the deployment needs none.

1. BACKUP + RESTORE TEST FIRST (owner, provider dashboard):
   take a full backup of the production Postgres database and
   TEST restoring it into a scratch database. Nothing in the
   repo can do or verify this. Do not deploy until it is done.
2. DEPLOY this package (v2.9.1) the usual way. Everything else
   happens by itself inside release_migrate when the new
   instance starts, in this order, under the migration lock:
   data preflight → the only migrations production has not seen
   (campaigns/0006 and story_intelligence/0001, 0002, 0003 — all
   additive: one nullable column on the articles table + new
   story_intelligence tables only; no existing table, row or
   field is altered, renamed or removed, and every pre-existing
   migration file is byte-identical to production's) → data
   postflight → the Story ID backfill (existing published
   articles receive their permanent IDs automatically) → the
   coverage report. Watch the deploy log if you like: it ends
   with the backfill result and "Coverage complete." — reading
   it is optional, acting on it is not required. If the backfill
   itself ever errored, the deploy would fail loudly before
   serving traffic; that is deliberate.
3. CLOUDINARY DESTROY VERIFICATION (one time, in the browser
   and the Cloudinary console — no commands): upload a
   throwaway test image as story evidence, run the story-desk
   redaction on it, and confirm in the Cloudinary console that
   the object is actually gone. This is the one check that can
   never be automated: it proves YOUR live Cloudinary account
   really deletes the file when the site asks — something only
   a person looking at the live account can confirm. (The
   redaction workflow itself refuses to proceed unless the
   provider confirms removal, and the code path is unit-tested;
   this step is about the account, not the code.)
4. SMOKE CHECKS (public site, in a browser):
   - an existing article page loads and shows the Story File
     panel with its Story ID;
   - /promises/ loads (empty tracker is fine — no seeded data);
   - /archive/ loads and a Story ID lookup resolves to its
     article;
   - /signals/ and /impact/stories/ load (empty lists are fine);
   - /about/ loads WITH the new KPN Media section and all its
     previous content;
   - the homepage, newsroom list and an opportunity page are
     unchanged.
5. Let the editorial GitHub Actions workflow run only AFTER the
   site deploy is confirmed healthy (it asserts migrations are
   deployed).
ROLLBACK: redeploy the previous (v2.4.4) package. The new column
and tables are inert to the old code and no existing row has been
changed; do NOT reverse the migrations or clear Story IDs.

# KPN Editorial Assistant — v2.8.0, Muse is the brain (auto-publish edition)

v2.8.0 (2026-11-01, KPN Media Phase 4 — Future Signal + Human
Impact Files): the two remaining editorial formats go live on the
Phase 1 spine. Both are ordinary Campaigns whose StoryProfile kind
— set by an editor in the existing story desk — is SIGNAL or IMPACT.
No new app, no new capability, no seeded content of any kind.

FUTURE SIGNAL is an evidence-led editorial analysis format, NOT a
prediction feed and never generated automatically. StoryProfile
gains signal framing fields (migration story_intelligence/0003):
signal_observation ("what we are seeing"), signal_confidence
(EMERGING / DEVELOPING / ESTABLISHED — how strong the EVIDENCE is,
in plain language), signal_horizon (the period looked at) and
signal_change_condition ("what would change this assessment"), plus
related_stories (M2M to published Campaigns the analysis is based
on). The four Phase 1 reader fields are reused for why it matters /
who is affected / what is unknown / what to watch. A signal whose
story has no related stories, claims or evidence records is flagged
on the story desk ("no evidence linked yet") — an editorial warning,
desk-only. Public surface: ONE new list page, /signals/, labelled
clearly as analysis ("not reports of established fact", "not
predictions"), and the article Story File panel renders the signal
framing in a distinct "Future Signal — KPN analysis" block with the
linked stories it rests on. One nav link ("Future Signal").

HUMAN IMPACT FILES carry the Phase 4 privacy core. StoryProfile
gains features_identifiable_people (editor-set flag) and an optional
impact_story link to core.ImpactStory. THE CONSENT GUARD lives in
story_intelligence.services (consent_block_reason /
ensure_consent_for_publish) and is enforced inside EVERY publish
transaction: campaigns.services.publish_campaign (web review queue +
REST API decision), editorial brain publish (BrainError code
'consent_required', item stays in REVIEW), editorial-desk handoff,
and archive restore. A kind=IMPACT story flagged as featuring
identifiable people CANNOT be published until at least one live
(not withdrawn) ConsentRecord is on file for it — any basis counts,
including an editor-recorded NOT_REQUIRED basis (the assessment
§7.6 mechanism). Anonymised impact files (flag off) and all other
stories publish exactly as before. The web and API publish endpoints
surface the guard's reason verbatim instead of a generic failure.
The guard checks only the EXISTENCE of a consent record — consent
contents are never read by it and never appear on any public page,
the archive or the API (asserted by tests). Public surface: ONE new
list page at /impact/stories/ ("Human Impact" nav link) using only
the editors' public profile fields. NOTE: /impact/ itself remains
core's long-standing organisational impact page — it is preserved
untouched and was NOT shadowed; Human Impact Files live under it.
Phase 1 emergency redaction works unchanged on impact stories
(verified by test). Tests: story_intelligence/tests/test_phase4.py
(28 tests).

DEPLOY NOTES FOR v2.8.0 (read before deploying):
0. OWNER DECISION (2026-11-01) STILL STANDS: DO NOT DEPLOY yet.
   Nasiru is holding all KPN Media phases and will deploy once
   every phase is complete. This package is a build milestone,
   not a release.
1. The v2.5.0 + v2.6.0 deploy notes still apply first (production
   backup + restore test; backfill_story_ids after deploy —
   SUPERSEDED by v2.9.1: the backfill is automatic on deploy,
   nothing to run by hand).
2. This phase ships exactly ONE new migration:
   story_intelligence/0003 — seven AddField operations on the
   story profile table (all blank/null/default, so every existing
   row stays valid untouched) plus one new M2M through table
   (story_intelligence_storyprofile_related_stories, a name that
   exists in no earlier migration). Every one of the 69 migration
   files from v2.7.0 is byte-identical; nothing is edited, removed,
   renamed or duplicated. Migration diff, collision audit and
   makemigrations --check were all run and are clean.
3. Rollback = redeploy the previous package; the new fields/tables
   are inert to older code and no existing row has been changed.

v2.7.0 (2026-11-01, KPN Media Phase 3 — Searchable Story Archive):
a new public discovery surface at /archive/ (story_intelligence
archive_urls, namespace 'archive'), linked as "Archive" in the main
nav next to Promise Tracker. It is a QUERY LAYER over records that
already exist — this phase adds NO models and NO migrations
(makemigrations --check is clean; every migration file is unchanged
from v2.6.0). Readers can text-search PUBLISHED stories (title,
subheadline, location, the plain-text body and Editor.js block text),
PUBLISHED story updates, and public promises (title, description,
institution name), with results grouped and labelled by type.
Typing an exact Story ID (KPN-SI-<YYYY>-<NNNNN>) resolves directly
to that story's article page; the Story ID chip on the article page
now links to the same lookup. Filters — category, senatorial zone /
LGA / ward (existing leadership geography), institution, date range
and content type — compose with the text query; each result section
paginates independently at 20 per page with all parameters
preserved. With no query, the Archive is a browse index of recent
published stories with their Story IDs. Public safety: only already
public records are ever queried — drafts, pending, returned and
archived articles, draft updates, private evidence, consent records,
audit internals and community-reporter data can never appear in
results, excerpts or counts (asserted by tests). Ordering: title
matches rank before body matches, then newest first. Only ORM
icontains/Q composition is used, so search behaves identically on
SQLite and Postgres (no database-specific full-text). The existing
newsroom list and its search are completely untouched, as are the
homepage, the REST API and every existing URL. Tests:
story_intelligence/tests/test_archive.py (43 tests).

DEPLOY NOTES FOR v2.7.0 (read before deploying):
0. OWNER DECISION (2026-11-01) STILL STANDS: DO NOT DEPLOY yet.
   Nasiru is holding all KPN Media phases and will deploy once
   every phase is complete. This package is a build milestone,
   not a release.
1. The v2.5.0 + v2.6.0 deploy notes still apply first (production
   backup + restore test; backfill_story_ids after deploy —
   SUPERSEDED by v2.9.1: the backfill is automatic on deploy,
   nothing to run by hand).
2. This phase ships ZERO migrations: release_migrate has nothing
   new to apply for the Archive. Deploying it cannot create,
   duplicate or alter any table or row.
3. Rollback = redeploy the previous package; /archive/ simply
   disappears and nothing in the database changes.

v2.6.0 (2026-11-01, KPN Media Phase 2 — Public Promise Tracker +
Community Radar): the story spine gains promises and community-report
linking. All new models live in `story_intelligence` (migration
story_intelligence/0002, CreateModel-only — it creates four new tables
plus one M2M table and cannot touch existing rows; every earlier
migration is unchanged). `Institution` (name, kind, internal contact
note/notes — never public). `Promise`: institution, what was promised,
announced_by/announced_on, source link, a NULLABLE deadline (only a
date the institution itself announced — never invented), geography via
the existing leadership Zone/LGA/Ward, M2M to Campaign stories, an
optional verification_claim, and next_check_date. Status workflow:
ANNOUNCED → DOCUMENTED → IN_PROGRESS → COMPLETED_VERIFIED, plus
DELAYED and DISPUTED. COMPLETED_VERIFIED is guarded in the service
layer (story_intelligence.services.set_promise_status /
record_promise_update): the editor must hold VERIFY_PROMISES and
verification evidence must be on record — a linked CONFIRMED claim
backed by an EvidenceRecord, or a promise update carrying an evidence
link. An official statement alone never counts. `PromiseUpdate` is the
append-only public timeline: every status change is written as an
update row plus a StoryAudit entry. Editorial promise desk at
/story/promises/ (overdue next-check dates flagged), gated by new
capabilities MANAGE_PROMISES / VERIFY_PROMISES (Director of Media,
Assistant, President, General Secretary — same grants as Phase 1).
Public tracker: ONE new page family, /promises/ (list with status
filter + detail with timeline, public evidence links and related
stories), linked from the main nav; public pages show reader-safe
fields only. Community Radar: `StorySourceLink` joins
core.CommunityReport → Campaign (kinds SOURCE / FOLLOW_UP / RELATED)
without modifying core; editors link/unlink from the story desk or
the Radar desk at /story/radar/ (which lists APPROVED reports not yet
linked to any story). Linking never changes a report, and a claim
recorded from a report ALWAYS starts NOT_ESTABLISHED — a community
submission never becomes a verified claim automatically. Privacy: the
public article Story File panel may show only geography-level
attribution for APPROVED linked reports ("reporting from KPN
community reporters in <LGA>"); reporter names, phone numbers,
internal notes and report text never appear on the article page, the
public tracker, or the API (asserted by tests). Brain API contract,
homepage and all existing URLs unchanged.

DEPLOY NOTES FOR v2.6.0 (read before deploying):
0. OWNER DECISION (2026-11-01): DO NOT DEPLOY yet. Nasiru is holding
   all KPN Media phases and will deploy once every phase is complete.
   This package is a build milestone, not a release.
1. The v2.5.0 deploy notes still apply first (production backup +
   restore test; backfill_story_ids after deploy — SUPERSEDED by
   v2.9.1: the backfill is automatic on deploy, nothing to run
   by hand).
2. Deploy applies story_intelligence/0002 automatically via
   release_migrate. It only creates new empty tables — no existing
   data is read, moved or rewritten.
3. Rollback = redeploy the previous package; the new tables are inert
   to old code.

v2.5.0 (2026-11-01, KPN Media Phase 1 — Living Stories + Truth Layers):
every published article now carries a permanent Story ID
(KPN-SI-<YYYY>-<NNNNN>, e.g. KPN-SI-2026-00142), and stories can grow
beyond the original article. New app `story_intelligence` owns the
story spine — StoryProfile (why it matters / who is affected / what is
unknown / what to watch), StoryUpdate (chronological developments),
Claim + EvidenceRecord + VerificationEvent (Truth Layers: a claim is
CONFIRMED only when KPN verified it; OFFICIAL_STATEMENT stays visibly
distinct), Correction (corrections are new linked records, never
silent rewrites), ConsentRecord (internal only, never public) and
StoryAudit. `campaigns.Campaign` gains ONE field, `story_id`
(migration campaigns/0006, additive + nullable). All five publish
paths allocate the ID inside the publish transaction (web queue and
REST API via the new campaigns.services.publish_campaign; brain
auto-publish and desk handoff inside their existing transactions) —
if allocation fails, the publish rolls back and the article stays
unpublished and recoverable. The article page gains a conditional
"Story File" panel that renders only when story data exists; nothing
else about the public site changes. Story desk for editors at
/story/<article-id>/, gated by new capabilities MANAGE_STORY_RECORDS /
PUBLISH_STORY_UPDATES / PERFORM_REDACTION (Director of Media,
Assistant, President, General Secretary). Archive-instead-of-delete:
published / Story-ID-bearing / story-record-bearing articles can no
longer be hard-deleted on web or API (owner spec 2026-11-01,
superseding the 2026-10-30 delete rule for those records) — archive
and restore actions preserve the ID and history; plain drafts delete
as before. Emergency redaction (PERFORM_REDACTION) replaces sensitive
text and destroys the underlying Cloudinary object, verifying the
provider's destroy response before saving; audit/correction records
never contain the sensitive material (the destroy code path is
unit-tested with a test double; verify once on the live account after
deploy with a throwaway upload). API serializers additively expose
story_id + source_name + source_url. Brain API contract unchanged —
brain-published articles simply also receive Story IDs.

DEPLOY NOTES FOR v2.5.0 (read before deploying):
1. OWNER ACTION FIRST: take a production database backup with the
   Postgres provider and RESTORE-TEST it into a scratch database.
   Nothing in this repo can do or verify that. Do not deploy before it
   is confirmed.
2. Deploy the package as usual (release_migrate applies the additive
   migrations campaigns/0006 + story_intelligence/0001 automatically).
3. [SUPERSEDED by v2.9.1 — kept only as the record of what the
   backfill command does.] AFTER the deploy is healthy, assign Story
   IDs to the existing published articles:
   `python manage.py backfill_story_ids --dry-run` first, then
   `python manage.py backfill_story_ids`. It is idempotent
   (safe to re-run; never renumbers) and touches nothing but story_id.
   Then `python manage.py check_story_id_coverage` should report
   "Coverage complete". Since v2.9.1 both commands run automatically
   inside release_migrate on every deploy — nobody runs them by hand.
4. Rollback = redeploy the previous package; the new column/tables are
   inert to old code. NEVER clear assigned Story IDs as a rollback.

v2.4.4 (2026-10-30, owner request): article sources are now a real
link BUTTON, not dead text. Campaign gains source_name/source_url;
both brain publish paths (publish.py auto-publish and the services.py
editorial handoff) store the source in those fields instead of
appending a plain-text "Source: ..." paragraph to the article blocks
(the plain-text `content` fallback keeps the line for non-HTML
surfaces). article_detail.html renders a green "Source: <name>" button
(external link, noopener) under the article body when source_url is
set. Migration campaigns/0005 backfills existing KPN_EDITORIAL
articles: it moves the trailing text source line out of their blocks
and content into the new fields, naming the source from the linked
editorial item when available, else the hostname.

v2.4.3 (2026-10-30, owner request): professional branded emails.
(1) Password-reset mail is now an HTML email with KPN header and a
"Reset Password" button (plain-text fallback kept), via staff/emails.py
+ staff/templates/staff/emails/password_reset.html. (2) Approving a
membership application in decide_application (web review AND mobile
API) now also emails the applicant a branded "Your KPN membership has
been approved" message with their role and an "Open Your Dashboard"
button — sent on transaction commit; a delivery failure is logged but
never blocks the approval. The in-app approval notification is
unchanged. (3) Sender advice: on Lockally, any address on a VERIFIED
domain can send without a mailbox existing, so LOCKALLY_FROM_EMAIL may
be noreply@kpn-staging.onrender.com; replies route automatically to the monitored
CONTACT_RECIPIENT_EMAIL (info@kpn-staging.onrender.com) with the KPN display name,
because the backend sets reply_to/reply_to_name.

v2.4.2 (2026-10-30, owner report): password-reset email was failing
("We could not deliver the reset email") even with Lockally credentials
set. Root cause was in `staff/lockally_backend.py`, not the env: it sent
`from`/`to` as {email, name} objects and no Idempotency-Key, while
Lockally's documented POST /v1/send requires `from` as a plain string,
`to`/`cc`/`bcc` as string arrays, a REQUIRED Idempotency-Key header,
and `reply_to` when the sender has no mailbox. The backend now follows
the documented contract exactly (reply_to falls back to
CONTACT_RECIPIENT_EMAIL), treats a 200 "suppressed" response as a
failure, warns loudly if a TEST (lk_test_) key is configured, and logs
the provider's problem detail on HTTP errors so Render logs name the
cause. Production still requires: LOCKALLY_API_URL =
https://api.lockally.com/v1/send, a LIVE lk_live_ key with send:write
scope, and the kpn-staging.onrender.com domain verified in Lockally (DKIM/SPF).

v2.4.1 (2026-10-30, owner report): editing a published article is now
save-only in the UI — the article editor no longer offers "Submit for
Review" for live articles (it shows "Save Changes (stays live)" and a
published notice instead), the submit-for-review URL answers published /
pending articles with a clear message rather than an error, returned
(REJECTED) articles can be resubmitted, and a "My Articles" quick-link
card now sits first on the Media Director dashboard.

## What changed in v2
- **OxionAI is removed entirely.** There is no third-party AI provider, API key,
  model name, endpoint lock or token budget anywhere in this app anymore.
  The old `editorial/provider.py`, its Usage/Budget accounting tables and the
  `test_editorial_ai` command are deleted.
- **Drafting intelligence is Muse**, the owner's external personal agent.
  Muse works outside the website, reads exported evidence, writes drafts,
  and returns them through a guarded inbox command. The website itself makes
  no AI calls.
- Everything else from v1 is unchanged: review-first, off by default, human
  handoff only, source allowlists, SSRF-safe fetching, audit trail.

## What this build does
- Discovers Kebbi news and opportunities from editor-approved RSS/Atom feeds or monitored single pages.
- Creates PRIVATE review items with source excerpts, URLs, dates and warnings.
- Drafting (news, opportunities, captions, and — only when explicitly enabled — member/community report wording) is done by Muse through the brain loop below. Suggestions never publish or overwrite anything by themselves.
- News handoff creates a Pending article authored by the human reviewer. An opportunity becomes public ONLY after an authorized human clicks "Approve and publish opportunity" and confirms fact checking.
- Deterministic jobs need no brain at all: daily briefings, duplicate alerts, deadline reminders at 7/3/1/0 days, bounded application-link checks, weekly summaries.
- Captions are suggestions only: no social account is connected and nothing is posted externally.

## Full automation: the Brain API (Muse connected directly)

The site exposes a small token-gated API so Muse can work as the
automation agent directly, with no files passed by hand:

- `GET  /editorial/brain/outbox/` — items waiting for a draft
- `POST /editorial/brain/inbox/` — Muse returns drafts; accepted drafts
  land in human REVIEW through the same validation as everywhere else
- `POST /editorial/brain/submit-item/` — Muse submits stories and
  opportunities it found by its own searching (news sites, Facebook and
  other social posts, official pages) with its draft attached; the item
  is created from the supplied evidence and lands in REVIEW. Duplicate
  source URLs are rejected
- `POST /editorial/brain/telegram-kb/` — Muse proposes Telegram bot
  knowledge-base entries; they are created INACTIVE and only answer
  members after the owner activates them in the Django admin
- `POST /editorial/brain/telegram/post/` — Muse posts an announcement
  to the KPN Telegram channel through the bot (the bot is a channel
  admin). Every post is recorded in the bot outbox (`BotOutbox`) and the
  audit log before it is sent
- `GET /editorial/brain/telegram/outbox/` and
  `POST /editorial/brain/telegram/drain/` — Muse verifies deliveries
  and sends anything still queued
- **Article auto-share:** when an article is published on the website
  (and the master switch is on), a share is queued and sent to the
  Telegram channel automatically — title, summary, link, once per article
- `GET /editorial/brain/telegram/questions/` and
  `POST /editorial/brain/telegram/answer/` — when the bot's instant
  providers cannot answer a member, the question is queued for Muse
  (the member is told it was passed to the media team); Muse answers
  through the API and the bot delivers the answer in the member's chat.
  Queued questions and answers are visible in the admin (`BrainQuestion`)
- `POST /editorial/brain/test-item/` — creates one private synthetic item
  so the whole loop can be tested on production safely

Rules (updated in v2.4, owner decision 2026-10-30): by default drafts
still require a human decision in `/editorial/` and the API cannot
publish. When the owner sets `KPN_EDITORIAL_AUTO_PUBLISH=True` on
Render, that changes on purpose: validated NEWS/OPPORTUNITY brain drafts
publish immediately (news → PUBLISHED article, opportunity → OPEN hub
entry), `POST /editorial/brain/publish-item/` can publish or repair a
REVIEW item with `draft_overrides`, and an editorial handoff goes
straight to PUBLISHED. The owner asked for this because scheduled runs
happen while he is away and review delays made posts late; he corrects
published items afterwards — editors (Director of Media, Assistant
Director, President, General Secretary) can now edit and delete ANY
article in the newsroom, including published ones. Synthetic items can
never publish either way, every read and write is written to the audit
log (`BRAIN_OUTBOX_READ`, `BRAIN_INBOX_WRITE`, `CLAUDE_DRAFT_READY`,
`CLAUDE_AUTO_PUBLISHED`), and setting `KPN_EDITORIAL_AUTO_PUBLISH=False`
restores review-first behaviour instantly.

Draft validation is stricter in v2.4, fixing two production errors:
a draft's `category` must be a real backend category for its kind (news
and opportunity category lists are different — `GENERAL` is news-only),
an opportunity `deadline` must be ISO `YYYY-MM-DD` in the future, and an
opportunity `provider` is required. The `/editorial/` draft form now
uses a category dropdown of the correct list, so a human can never save
a category the handoff would reject either. A NEWS draft may carry
`image_url` (https) or `image_base64`; the image becomes the article's
featured image at publish time, and a bad image never blocks the story.
Public article pages no longer show the publishing person's name or
photo — only the reporter credit, "KPN Editorial Team", with nothing
under it.

Duplicate protection (v2.4): the same story or opportunity must never
publish twice. `GET /editorial/brain/published/` lists live articles,
open opportunities and tracked brain items so Muse checks before
submitting; submit-item rejects an exact title/application-link match
(`duplicate_candidate`) even from a different source URL; at publish
time an exact duplicate is dismissed (`CLAUDE_DUPLICATE_DISMISSED`) and
a near-duplicate title (0.85–0.92 similar) is blocked with
`possible_duplicate` and left in REVIEW for a human decision.

### Activation (one time)

1. Generate a long random token (at least 32 characters), e.g. with a
   password generator. Never send it in chat or email.
2. On Render, set the environment variable `KPN_EDITORIAL_BRAIN_TOKEN`
   to that token and redeploy. If the variable is empty or absent, the
   Brain API endpoints do not exist at all (404).
3. Give the same token to Muse through Muse's secure credential
   entry only. Muse stores it in secure storage, never in code or chat.
4. Muse then runs scheduled checks against the API (after each worker
   run), drafts everything waiting, and returns the drafts automatically.
   The owner only reviews.

### Testing on production (never run the test suite there)

`python manage.py test` is a local/CI tool — it builds and destroys a
test database and must never run against production. The safe
production test is the synthetic loop, which needs no shell:

1. With the token set and editorial enabled, Muse calls
   `POST /editorial/brain/test-item/`, then reads the outbox, drafts the
   synthetic item, and posts it to the inbox.
2. The owner opens `/editorial/` and sees the synthetic draft in REVIEW,
   then dismisses it. Synthetic items cannot be handed off or published.
3. If anything fails, pause automation in `/editorial/` and set
   `KPN_EDITORIAL_ENABLED=False`; no draft can enter while paused.

The file-based loop below remains available as a fallback (for example
before the token is configured).

## The brain loop (how Muse drafts)
1. The scheduled/manual worker (`run_editorial`) runs DISCOVER jobs and
   maintenance. GENERATE jobs are **never** run inside the website — they
   stay QUEUED for the brain.
2. On a machine with the production environment configured, run:
   `python manage.py brain_outbox --output brain-outbox.json`
   This exports WAITING items (id, kind, version, source, evidence) as JSON.
   Report evidence is contact-redacted before export, and report items are
   only exported when `KPN_EDITORIAL_REPORT_DRAFTING_ENABLED=True`.
3. Give the file to Muse. Muse returns a JSON list of
   `{item_id, version, draft}` entries, drafted under the same rules the
   workspace enforces: supplied facts only, no invented names/dates/figures,
   original wording, allegations flagged in notes.
4. Import: `python manage.py brain_inbox brain-inbox.json`
   Each draft is validated (allowed fields per kind, required content,
   opportunity application link must come from the source evidence, caption
   length, item still WAITING at the exported version, report unchanged
   since export). Accepted drafts land in **REVIEW** with a
   `CLAUDE_DRAFT_READY` audit entry. Rejections change nothing and say why.
5. A human reviews in `/editorial/`, edits if needed, and hands off or
   dismisses — exactly as before.

Test the loop safely with `python manage.py test_brain_loop`: it creates a
private synthetic WAITING item (no external request). Watch it travel
outbox → draft → inbox → dismiss. Synthetic items cannot be handed off.

## Deploy
The feature is OFF by default in both environment and database.
New in v2: migration `editorial/0002_remove_provider_accounting.py` deletes
only the old Usage/Budget tables. Deploy with the normal production commands
with `KPN_EDITORIAL_ENABLED=False` initially, apply migrations, then
configure:

| Key | Value / meaning |
|---|---|
| KPN_EDITORIAL_ENABLED | True after migration; False is a server-side kill switch |
| KPN_EDITORIAL_REPORT_DRAFTING_ENABLED | False initially; True lets Muse draft wording for member/community reports (owner privacy decision) |
| KPN_EDITORIAL_NOTIFICATIONS | False initially; True enables private review inbox alerts and existing FCM attempts |
| KPN_EDITORIAL_DAILY_DRAFTS | 5 per type (news/opportunities) per day from discovery |
| KPN_EDITORIAL_BRAIN_TOKEN | Long random secret (32+ characters) for Muse's Brain API access; empty = API does not exist. Set on Render only, share with Muse via secure credential entry only |

There are **no AI keys to set on Render or in GitHub Secrets anymore.**
If you previously created `KPN_EDITORIAL_AI_KEY` secrets or
`KPN_EDITORIAL_AI_MODEL` variables anywhere, delete them.

## Scheduling
Unchanged: `.github/workflows/kpn-editorial.yml` runs the bounded worker
3×/day (07:17 / 15:17 / 23:17 Africa/Lagos) plus manual dispatch. It needs
DATABASE_URL + Cloudinary secrets only (Firebase JSON optional, for review
push). GitHub schedules are best-effort and can be delayed; drafting happens
when Muse checks the Brain API on its own schedule, not inside the runner.

## Private workspace
`https://kpn-staging.onrender.com/editorial/` — Media Director/superuser manage
sources and pause/resume; existing article-editor roles see global drafts;
opportunity publication still requires MANAGE_HUBS; report items keep their
existing supervisor boundaries. The index page shows how many items are
waiting for Muse.

## Privacy
Report originals and evidence stay in the editorial tables; no retention
deletion is automatic — set an organisational retention policy before volume
use. Drafts are written by the owner's own agent; no content is forwarded to
any third-party AI provider by this app. Human confirmation and source
comparison remain essential against errors, scams, defamation and copyright
issues.

## Tests
`editorial/tests/` covers the app end to end (149 tests across editorial and telegram_integration): access scoping,
fetch safety, discovery, brain outbox/inbox validation, the Brain API
(auth, round trip, unconfigured/disabled states), worker behaviour
(including that GENERATE jobs are never executed in-site), signals,
maintenance, services handoffs and the workspace views. Run:
`python manage.py test editorial`

## Recovery and rollback
Pause in the workspace and set the Render flag False; disable the GitHub
schedule variable too. To roll back code, pause first, stop the runner,
restore the previous source and leave the editorial tables in place for
audit/recovery.
