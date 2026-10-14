> Latest lifecycle acceptance follow-up: 92 local backend tests passed; report locking/escalation, push-token account ownership and audit-author protections corrected. Public release remains blocked pending operational acceptance. Start with `release-evidence/lifecycle-acceptance/RELEASE_GATE_STATUS.md`. Earlier verification sections are retained as historical evidence.

> Latest follow-up: API hardening and expanded local verification. Use `release-evidence/expanded-verification/summary.json` for current results; earlier verification counts below are historical base-candidate evidence. This package remains a staging candidate, not public-release approval.

# KPN 1.2.4 — deployment release candidate

Website/backend and Android source are included. This supersedes the earlier Phase 1 package. **Not yet deployed or signed; not a certification of every legacy workflow.** Start with `docs/DEPLOYMENT.md` and `docs/RELEASE_ACCEPTANCE.md`.

## Confirmed release rules
- Hosting: existing Render service, Neon PostgreSQL, Cloudinary uploads.
- Leadership-only applications; General-member signup stays disabled.
- Verified, active Director of Media & Communications retains full management capabilities. Other roles are not elevated.
- Rejecting an applicant archives the account, disables sign-in and preserves related records. Historical deletions cannot be recovered by this change.
- Keep the existing Android package and release signing certificate. Version candidate: 1.2.4 / code 7; confirm the highest distributed code before signing.

## Implemented
- Shared web/API registration validation, photo limit, leadership location checks and transactional account creation.
- PostgreSQL seat constraints, locked approvals and safe swaps/reinstatement; preflight blocks inconsistent existing assignments rather than deleting them.
- Archive actor/time/reason, correct Verified/Archived directory labels, compatible API archive timestamp, and truthful native rejection confirmation.
- Auth input validation, JWT-protected password changes, inactive-account access denial, safe announcement rendering, sanitised published content and fail-closed Telegram webhook authentication.
- Strict production configuration: no SQLite/local-upload/console-email fallback; HTTPS/secure cookies; readiness endpoint; locked startup migrations.
- Locked Python runtime dependencies, self-hosted website assets, shared branding/appearance choices, responsive public and account surfaces, private-safe offline screen.
- Exact accessible membership-period counts replace the unreadable legacy graph; periods no longer double-count shared boundaries. Counts group currently verified accounts by registration, not approval date.
- Render build/start scripts, configuration examples, backend/native checks and manual secure-signing CI workflow. No service was created and no paid resource was purchased.

## Verification
See `release-evidence/summary.json` and the corresponding logs. Tests use isolated synthetic records; no QA accounts, database dumps, credentials or mock services are part of runtime source.

The production settings check verifies configuration shape, **not** provider delivery. A local PostgreSQL restore test does not prove a real Neon backup can be restored. Native unit tests/build/lint do not prove physical-device acceptance. Historical Android evidence under `Kpn_android/verification` is not evidence for this candidate.

## Important limitations before go-live
Complete the acceptance checklist: production-backup restore, actual providers, role/workflow parity, legal policies, device acceptance, signing/update compatibility, monitoring and operational approval. No signed release APK is included. The sandbox emulator could not complete boot reliably without hardware acceleration, so instrumentation/device execution remains unverified. Do not distribute a debug or emulator-API build.

## Development
Python 3.11.15; `requirements.lock.txt` is the deployment lock. `docs/legacy-uv.lock` is historical and must not be used to install this release. JDK 17 / Android SDK 35 / included Gradle wrapper for Android.

Rebuild website utilities when templates change:
```bash
npm ci
npm run build:css
python manage.py collectstatic --noinput
```
Compiled CSS and vendor licences are included. Keep credentials, local databases, SDKs, Gradle caches, keystores and build outputs out of source control.
