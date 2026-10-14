# State announcements and dashboard quick actions

## Audience

- General: all verified, active KPN members (existing behavior retained).
- State: verified, active state executive team members only; lower-tier leaders and general members are excluded. Unassigned, pending, suspended and superuser accounts are not included in this audience.
- Only state-level senders can select State. Zonal, LGA and Ward sender scopes remain restricted to their existing jurisdiction.
- State uses no zone/LGA/ward target. Website and Android source show explicit audience labels.
- Test announcements on the deployed service must remain inactive; no live broadcasts are required for deployment checks.

## Quick actions

All role website dashboards use the same permission-filtered component: four short icon-and-label tiles, with remaining authorized tools behind the native, keyboard-accessible More actions disclosure. Statistics, responsibilities, profile sections and work queues were not redesigned. The stylesheet URL and safe-static worker cache are versioned so existing installed website clients fetch the updated styles.

The action catalog follows existing web permissions rather than granting web access from a broader API capability. Regression checks cover dashboard rendering and the actual GET destination of every offered action for all 41 leadership roles plus the general member.

## Verified fixes

- State Media Director retains report oversight but no longer advertises a scoped report-submission capability with no valid recipient.
- Restored three legacy web role-title aliases used by assistant programme/events, youth and public-relations views.
- Currency summary outputs consistently use two decimal places across database engines.
- SMTP requests have a bounded timeout. Contact-form delivery uses a configurable recipient and no longer returns success after an unsuccessful provider send.
- PWA install button contrast is increased; the prompt has a named region landmark.

## Verification and remaining limits

Local backend suite: 118 tests completed, 116 passed and two PostgreSQL-only concurrency tests skipped on SQLite. Schema dry-run detected no missing migrations.

Local responsive previews are captured separately. Image inspection was unavailable because the image-view helper failed; automated layout diagnostics must not be described as a completed visual/taste review.

Render's free plan blocks outbound SMTP ports, including Gmail's port 587. A timeout fix provides an honest failure response, not a working email provider. Restoring real email delivery requires an approved compatible provider configuration or a user-approved hosting plan change. No paid change was made.

Android announcement-screen source includes State, but this change is not an installed or newly signed Android release. Rebuilding and device testing remain separate gates.
