# Release acceptance — outstanding operational gates

Local automated results are in `release-evidence`. Mark these items only after actual execution and keep evidence outside public source when it includes personal data.

## Critical — block public release
- [ ] Restore the real pre-change Neon backup into an isolated destination; confirm relationships and rollback/reconciliation plan. Local synthetic restore is not enough.
- [ ] Confirm existing seat integrity and approve any necessary manual corrections. No automatic conflict deletion.
- [ ] Staging: leadership-only signup, all location dependencies, valid/invalid photos, duplicate identities, vacancies, approval/rejection/archive, concurrent decisions, role changes/swaps/suspension/reinstatement, and denied out-of-scope actions.
- [ ] Verify Media Director's intended full management access and all other role/tier restrictions on both clients. Full access does not mean impersonating another officer or bypassing workflow validation.
- [ ] Verify actual Neon TLS/connectivity, persistent Cloudinary uploads after restart, real email reset delivery, Telegram linkage/channel verification and webhook secret.
- [ ] Exercise signed app login/logout/refresh/reset, protected dashboard access, uploads, drafts, errors/retry, notification permissions/delivery and update over the current installed app on real Android devices.
- [ ] Preserve signing certificate; publish a version code greater than every distributed version. No debug/emulator build distribution.
- [ ] Review/approve privacy notice, retention/archiving, member data/photo handling, consent and support processes before collecting public applications. Code of Conduct is not a privacy policy or Terms.
- [ ] Perform targeted security review of remaining legacy/API mutations and shared multiworker abuse protection. Existing local-cache throttles are not a global quota; assess shared cache/edge protection before public exposure.

## Normal — complete before declaring full parity
- [ ] Run the complete role/workflow matrix for reports, editorial/media moderation, events/minutes, finance, programmes, welfare, disciplinary and export operations, including older clients.
- [ ] Review all legacy screens at mobile/tablet/desktop, light/dark, empty/error/loading and keyboard/screen-reader states. Sampled screenshots do not certify whole-product WCAG compliance.
- [ ] Confirm canonical branding, real contacts/social links, leadership records, opportunities, and content ownership. Do not deploy synthetic QA stories/accounts.
- [ ] Verify physical-device Firebase push using the matching client/Admin project, real Telegram notifications and signed-download hosting.
- [ ] Assign alert ownership, backup frequency, retention, incident response, migration approval and release support.

## Low — after launch approval
- [ ] Further legacy screen cleanup, approved real community photography, editorial style and performance tuning from measured usage.
- [ ] Periodic dependency/security updates and device/role regression runs.

## Known verification boundaries
This candidate has local PostgreSQL regressions, configuration checks, sampled browser checks and native unit/build/lint evidence. Real production services were not accessed. Android instrumentation was compiled but could not be executed successfully because the software-only sandbox emulator did not finish boot reliably. Do not mark device acceptance passed. Neither the CI workflows nor private signing steps have been run in the user's repository.
