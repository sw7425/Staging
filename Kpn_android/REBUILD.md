# KPN Android 1.2.4 — build and signing

## Identity and toolchain
JDK 17, Android SDK Platform/Build Tools 35, included Gradle wrapper 8.11.1, AGP 8.7.3, Kotlin 2.0.21. Set `ANDROID_HOME` privately/local to your SDK. No SDK/cache/local.properties/private key is supplied.

- Application ID: `com.kpn.mobile`
- Candidate version: 1.2.4 / code 7
- Minimum Android: API 26; target/compile: 35
- Release API: `https://www.kpn.com.ng/api/v1/`; HTTPS, non-debuggable

Confirm the highest already distributed code. If it is 7 or higher, increase `versionCode` and the candidate version deliberately, rerun checks, then sign. Never uninstall a production app to bypass an update/signature failure.

The supplied historical public certificate SHA-256 is:
`EB:66:89:4D:81:2F:21:B4:7F:92:7B:A0:52:F0:0B:FD:47:B4:57:2D:AA:9D:FD:63:EC:80:80:5A:F6:1C:04:82`

## Private local signing
Configure using your password manager/local secure terminal, not chat/source:
- `KPN_KEYSTORE_FILE`: absolute path to the existing keystore
- `KPN_KEYSTORE_PASSWORD`
- `KPN_KEY_ALIAS`
- `KPN_KEY_PASSWORD`: optional only if identical to store password
- `KPN_HIGHEST_RELEASE_CODE`: highest code already distributed

From repository root:
```bash
bash scripts/build-android.sh release
```
This runs release unit/lint checks, creates the signed APK, checks its signature against the supplied certificate, package ID, higher version code and alignment. Packaging without the key fails deliberately. Output: `Kpn_android/app/build/outputs/apk/release/app-release.apk`. Verification is still required after any minification change; default release minification remains off.

## GitHub private signing alternative
The supplied `.github/workflows/android-release.yml` runs manually. Configure the `android-release` Environment with approval controls and these private secrets: `KPN_KEYSTORE_BASE64`, `KPN_KEYSTORE_PASSWORD`, `KPN_KEY_ALIAS`, and `KPN_KEY_PASSWORD` if different. Base64 is only encoding, not encryption; treat it as a private key. Encode/configure it locally without pasting into chat. No secrets are needed for `.github/workflows/quality.yml`.

Run the signing workflow with the confirmed highest distributed code. It uploads only the APK after checks and removes the temporary key. Review the workflow/security controls before running; it has not been executed in the owner's repository. Back up the existing key securely; do not generate a replacement for an update.

## Isolated development
```bash
cd Kpn_android
bash gradlew --no-daemon :app:testReleaseUnitTest :app:lintRelease :app:assembleDebug :app:assembleDebugAndroidTest
```
Debug uses the normal canonical API unless explicitly overridden with `-PdebugApiBaseUrl=http://10.0.2.2:8000/api/v1/`. Never distribute a debug/emulator build. `NativeApprovalTest` requires a separately prepared isolated Django fixture server (IDs 3/4/5 for distinct vacant-seat applicants, President/Media Director synthetic accounts, and QA-only 503 arm route). Do not expose the QA route on production. Other instrumentation fixtures are local fake states. Compilation is not execution.

## Launch acceptance
Deploy the matching backend first; this release has real archive/seat/auth changes, unlike old Android-only notes. Verify older clients, the new archive response, Media Director access and actual device workflows. Test an in-place update over the current signed app.

`google-services.json` is Firebase Android client configuration, not a server service account. Configure Firebase Admin privately on Render and verify push delivery on a physical device. No private key, signed APK or real-device acceptance is included in this source package. Existing `verification/` records describe previous releases only.
