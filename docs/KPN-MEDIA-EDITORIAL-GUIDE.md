# KPN Media — Editorial Guide

The practical operating guide for the KPN editorial team. It covers
how we verify, correct, follow up and protect people on kpn-staging.onrender.com —
the daily practice behind Living Stories, Truth Layers, the Public
Promise Tracker, Community Radar, Future Signal, Human Impact Files
and the KPN Knowledge Archive.

Keep this guide beside the setup document
(`docs/EDITORIAL-ASSISTANT-SETUP.md`, which covers the technical
system). This one covers editorial judgement and procedure.

---

## 1. The golden rules

1. **Never publish what we cannot stand behind.** If it is not
   verified, label it for what it is (see §2) or do not publish it.
2. **An official statement is never, by itself, a confirmed fact.**
   It is a statement by an authority — nothing more until evidence
   confirms it.
3. **Never silently rewrite history.** Corrections are public
   records. Developments are added as story updates.
4. **Protect people first.** Privacy and safety action (redaction)
   never waits for a meeting, a queue or a routine review cycle.
5. **Never publish a community reporter's private details** — no
   names, no phone numbers, ever.
6. **No consent, no identifiable person** in a Human Impact File.
7. **Published stories are archived, never deleted.**
8. **Do not invent.** No invented figures, names, dates, deadlines,
   quotes, progress percentages or verification results — in
   articles, promises or updates.

---

## 2. Claim statuses — the Truth Layers vocabulary

Every important claim on a story carries one status. The status is
shown to readers in plain language on the article's Story File
panel, and every change of status is written into the story's
verification history.

| Status | Reader label | Meaning |
|---|---|---|
| `CONFIRMED` | Confirmed by KPN | KPN has checked this against evidence (documents, records, direct reporting, reliable corroboration) and stands behind it. |
| `OFFICIAL_STATEMENT` | Official statement — not independently verified by KPN | An authority (government, agency, institution, company) has said this. KPN has **not** independently verified it. |
| `DEVELOPING` | Still developing — KPN is following this up | Early or partial information; evidence is still being gathered. |
| `DISPUTED` | Disputed — sources disagree | Credible sources conflict. Say who says what; do not pick a winner without evidence. |
| `NOT_ESTABLISHED` | Not yet established | The claim exists (e.g. it came from a community report or social media) but nothing reliable supports it yet. |

Rules:

- A claim may move **up** (e.g. OFFICIAL_STATEMENT → CONFIRMED)
  only when evidence has actually been added to the story — add
  the evidence record first, then change the status.
- A claim must move **down** or to DISPUTED the moment credible
  doubt appears. Downgrading is not an embarrassment; hiding doubt
  is.
- The article itself also carries an overall verification status
  (Developing / Confirmed / Verified / Community Alert), set from
  the story desk. Keep it consistent with the claims underneath it.

---

## 3. Verification practice

Before a story or claim is marked CONFIRMED:

- [ ] Add at least one **evidence record** on the story desk
      (`/story/<article-id>/` → Evidence): a source link, document,
      photo, video or official record, with a title that says what
      it proves.
- [ ] Mark evidence **private** when it must not be public (e.g. a
      document containing personal data). Private evidence is
      never shown on the public article page — but it still counts
      for verification internally.
- [ ] Fill the article's **source name and source link**. Readers
      see them as the green **Source button** under the article —
      our public promise that the reporting can be traced.
- [ ] For a second source, prefer an independent one (a different
      outlet, an official record, a named eyewitness account) over
      a repeat of the same report.
- [ ] Record who is affected and what remains unknown in the story
      profile ("why it matters / who is affected / what is unknown
      / what to watch"). Honest unknowns build trust.

Never copy a figure, name or date from a single social-media post
into a CONFIRMED claim.

---

## 4. Corrections, clarifications and story updates

Three different tools — choose the right one:

- **Story update** (story desk → Updates): the story has *moved
  on* — a new development, an official response, a follow-up
  report, a community update. Updates are published in
  chronological order under the original article. Published
  updates are historical records: they are never silently edited
  afterwards.
- **Correction** (story desk → Corrections, kind = Correction):
  something we published was *wrong* — a name, figure, date,
  place. The correction states plainly what was wrong and what is
  right. Fix the body text **and** publish the correction; the
  correction record is the public trace.
- **Clarification** (kind = Clarification): what we published was
  technically right but reasonably misunderstood. The
  clarification adds the missing context.

Rules:

- If a substantive fact in the article body changes, the change
  must be matched by a correction record or a story update. A
  quiet edit that changes the meaning of a published story is
  prohibited.
- Typo and formatting fixes that change no meaning may be edited
  directly.
- If the original reporting stands but events have moved, do not
  edit the body to pretend we always knew — add an update.

---

## 5. Story IDs

- Every published story receives a permanent **Story ID**
  (`KPN-SI-<year>-<number>`, e.g. `KPN-SI-2026-00042`) at the
  moment of first publication, on **every** publication path —
  web newsroom, mobile API, editorial desk handoff and the
  automated brain. Publishing fails safely if an ID cannot be
  allocated; the article stays unpublished and recoverable rather
  than going live without an ID.
- An ID never changes and is never reused, including after a story
  is archived and restored.
- Readers can type a Story ID into the Knowledge Archive
  (`/archive/`) to go straight to the story.
- **Backfill** is AUTOMATIC (v2.9.1): nobody runs anything by
  hand. On every deploy, `release_migrate` runs
  `backfill_story_ids` right after the migrations (inside the
  same lock), followed by the `check_story_id_coverage` report
  in the deploy log. The backfill assigns IDs to already-
  published articles oldest-first, skips any article that
  already has one, changes nothing else, and is a fast no-op
  once coverage is complete — so a story published by the old
  code during a deploy window receives its ID on the next
  deploy at the latest (the audit backstop also flags it in
  the meantime).

---

## 6. Public Promise Tracker rules

Public pages: `/promises/`. Desk: `/story/promises/`.

Statuses: **Announced → Documented → In Progress → Completed —
verified**, with **Delayed** and **Disputed** where they apply.

- Record only promises that were genuinely made publicly, with
  the source (name + link) and the date announced. **Never invent
  a deadline**: if no date was promised, the deadline stays empty.
- Add a **next-check date** to every active promise and work the
  overdue list — the desk flags promises whose check date has
  passed. On each check, add a promise update: what was found,
  with an evidence link where there is one.
- Every status change is written as a dated update on the public
  timeline. The history is append-only — never edit an old update
  to change what it said.
- **Completed — verified** is guarded by the system and by this
  rule. It requires:
  1. an editor holding the **VERIFY_PROMISES** capability, and
  2. verification evidence on record: either a linked claim on a
     related story with status CONFIRMED and at least one evidence
     record, **or** a promise update carrying an evidence link.
  If neither is present, the system refuses the status. Do not
  work around it — gather the evidence first.
- Institution contact notes and internal notes are never public.

---

## 7. Community reports — Community Radar

Desk: `/story/radar/`. Reports arrive through the existing
community-report intake and keep their existing review workflow.

- A community report is a **lead, not a fact**. Nothing in a
  report is published as verified news until an editor has
  reviewed it and the normal verification practice (§3) has been
  applied.
- Link a report to a story from the story desk when it genuinely
  contributed. Publicly, readers only ever see a geography-level
  line ("with reporting from community reporters in <LGA>").
- **Never publish or share a reporter's name, phone number or
  other private details** — not on the article page, not in the
  archive, not in social posts.
- When a report is turned into a claim on the story desk ("record
  as claim"), the claim **always starts at NOT_ESTABLISHED**. A
  submission never becomes a verified claim by being submitted.
- Reports that cannot be verified stay in the reporting system;
  they do not appear in the public archive.

---

## 8. Human Impact Files and consent

Public page: `/impact/stories/`. A Human Impact File is a story
whose profile kind is set to IMPACT in the story desk.

Consent rules:

- If a story **features identifiable people** (they can be
  recognised — name, face, voice, or enough detail to identify
  them), set the profile's *features identifiable people* flag
  and record consent on the story desk (→ Consent) **before**
  publishing.
- Accepted bases of consent: **Written**, **Verbal (recorded)**,
  **Guardian** (for a minor — always guardian consent), or
  **Not required** (recorded by an editor only where the person
  is genuinely not identifiable, e.g. fully anonymised accounts
  or public officials speaking in their official capacity).
- **The publish guard:** an IMPACT story flagged as featuring
  identifiable people **cannot be published** — on the web, the
  API, the editorial desk, the automated brain, or by restoring
  an archived copy — until at least one live consent record
  exists for it. The block message states the reason; do not try
  to bypass it.
- **Withdrawal:** if a person withdraws consent, mark the consent
  record *withdrawn* immediately. The story can no longer be
  re-published or restored until the situation is resolved, and
  consider redaction (§9) of the identifying material.
- **The anonymised alternative:** where consent cannot be
  obtained, tell the story without identifying details (no name,
  no recognisable photo, changed identifying details where that
  does not distort the facts), leave the flag off, and note the
  anonymisation in the story profile.
- Consent records are **internal only**. The internal subject
  descriptor (e.g. "market trader, Argungu — interview 12 Oct")
  is a newsroom reference, never a public name, and consent
  details never appear on any public page or in the API.

---

## 9. Emergency redaction

Use redaction when published material puts a person at risk or
exposes private information: personal data exposed, safety of a
person, a legal requirement, or consent withdrawn.

**Act immediately.** Protection does not wait for a second review
or a routine cycle. The redaction action sits on the story desk
(→ Redact) and requires the **PERFORM_REDACTION** capability.

Procedure:

1. Open the story desk for the article and choose Redact; pick
   the target — the article, a story update, a claim, or an
   evidence record — and the reason category.
2. The system first destroys any underlying uploaded file in
   Cloudinary and **verifies the provider's confirmation**. If
   removal cannot be confirmed, nothing is changed and an error
   is shown — escalate to the Director of Media at once; do not
   assume the file is gone.
3. Only after file removal is confirmed does the system replace
   the sensitive text and save.
4. The system writes a public **Redaction** correction notice and
   an audit entry that describe the action and the reason
   category only. **Never** write the sensitive material itself
   into a correction, an audit note, a chat message or a report.
5. Afterwards, check the public article page and the archive to
   confirm the material no longer appears, and tell the Director
   of Media what was removed and what was verified.

Keep the redaction proportional: remove the identifying material,
not the whole story, unless the whole story is the risk.

---

## 10. Future Signal

Public page: `/signals/`. A Future Signal piece is a story whose
profile kind is set to SIGNAL in the story desk.

- Future Signal is **analysis, not news and not prediction**. The
  public page and the article panel label it as KPN analysis.
- Frame every signal in the profile: *what we are seeing* (the
  observation), *why it matters*, *who is affected*, *what is
  still unknown*, *what to watch*, the period it covers, and
  *what would change this assessment*.
- The **confidence** field describes the strength of the
  **evidence** — Emerging (early signs, limited evidence),
  Developing, or Established. It is never a probability and never
  a promise that something will happen.
- A signal must rest on evidence: link the published stories it is
  based on, and/or the story's claims and evidence records. The
  story desk flags a signal with no evidence linked — fix the flag
  before publishing.

---

## 11. Archive instead of delete

- A **published** article, an article that has a Story ID, and an
  article with story records (updates, claims, evidence,
  corrections) attached **cannot be deleted** — on the website or
  through the API. To take it off the public site, **archive**
  it. Archiving preserves the Story ID and the full history, and
  the article can be **restored** exactly as it was.
- Plain drafts with no Story ID and no story records can still be
  deleted as before.
- Archiving is a visibility decision, not a privacy remedy: if
  the problem is sensitive content, use redaction (§9) — and
  archive as well if the story must come down while redaction is
  arranged.
- Restoring an archived Human Impact File re-runs the consent
  guard (§8).

---

## 12. Who may do what

Story and promise work is governed by capabilities in the existing
leadership permission system, enforced on the server for every
action (hiding a button is never the protection). The story
capabilities are held by the **Director of Media &
Communications**, the **Assistant Director of Media &
Communications**, the **President** and the **General Secretary**.

| Capability | Allows |
|---|---|
| `MANAGE_STORY_RECORDS` | Edit story profiles; add claims, evidence records, corrections and consent records; link community reports; set a story's kind (news / signal / impact) |
| `PUBLISH_STORY_UPDATES` | Publish story updates and corrections |
| `PERFORM_REDACTION` | Run emergency redaction (§9) |
| `MANAGE_PROMISES` | Create and edit institutions and promises; add promise updates; change promise status (except verified completion) |
| `VERIFY_PROMISES` | Mark a promise **Completed — verified** (with the evidence of §6 on record) |

If a task needs a capability you do not hold, route it to a holder
— never share logins.

---

## 13. The automated brain runs (Muse)

Muse, the editorial assistant, discovers Kebbi news and verified
opportunities and publishes under standing owner instructions.
The team's single reference for those rules:

- **Schedule discipline.** Publishing happens only inside the
  four daily runs — **08:00, 13:00, 17:00 and 21:30**
  (Africa/Lagos). Nothing is published off-schedule unless the
  Director of Media explicitly changes the rule.
- **Recent news only.** A news item qualifies only if the source
  article's own publication date is within the preceding 24
  hours. Old news is never presented as new.
- **No duplicates.** Every run first retrieves the website's
  published list. Duplicates are judged by the **event** — the
  same person and achievement, the same announcement, or the
  same scholarship/grant (same provider or same application
  link) is a duplicate even under different wording.
- **Opportunities.** Eligibility is checked state by state —
  only opportunities open to Kebbi indigenes are published. The
  **application link is copied exactly from the verified
  evidence** (the source/listing URL); a derived or guessed link
  is rejected by the system and must never be worked around. A
  provider and a real future deadline are required.
- **Byline.** The public byline is always **KPN Editorial Team**.
- **Images.** The genuine source photo is used where available.
  A fabricated photograph of real people is never used; where no
  verifiable image exists, publishing without one is preferred.
- **Expired opportunities.** An opportunity stays open for its
  whole deadline day and is closed from the following day.
- **Editorial automation limits.** Member and community *report*
  drafting by the assistant stays off unless the Director of
  Media enables it.

What the team does with the runs:

- Read each run's report in the automation chat; follow up any
  item that landed in REVIEW instead of publishing.
- Items the assistant submits carry the same standards as any
  editor's work: if a published item is wrong, correct it through
  §4 — do not quietly edit it.

---

## 14. Quick checklists

**Before you publish a story**

- [ ] Claims labelled with the right statuses (§2)
- [ ] Evidence records added; source name + link filled (§3)
- [ ] Story profile filled: why it matters / who is affected /
      what is unknown / what to watch
- [ ] If it features identifiable people and is a Human Impact
      File: consent on record (§8)
- [ ] If it is analysis: kind set to SIGNAL and framed as such
      (§10)

**When a story develops**

- [ ] Add a story update — do not rewrite the original (§4)
- [ ] Re-check claim statuses against the new information (§2)
- [ ] Update any linked promise and its next-check date (§6)

**When something is wrong**

- [ ] Factual error → correction record + fix (§4)
- [ ] Person at risk / private data exposed → redact immediately
      (§9), then inform the Director of Media
- [ ] Story must come down → archive it (§11); never delete a
      published story

---

*KPN Media Editorial Guide — issued with KPN Media v2.9.0
(2026-11-01). Amendments are made by the Director of Media &
Communications and recorded here.*
