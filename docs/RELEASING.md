# Releasing

How to build a signed APK and publish it on GitHub. Written for Android Studio, since the repository
does not include the Gradle wrapper scripts.

## 1. Create a signing key (once)

*Build > Generate Signed App Bundle or APK > APK > Next > Create new...*

- Save the keystore (`.jks`) **outside the repository**, for example in a private folder.
- Choose strong passwords and write down the alias.
- **Back the keystore up** (password manager or encrypted drive). If you lose it, you can no longer
  publish updates that install over the old version: users would have to uninstall first.
- **Never commit it.** `.gitignore` already blocks `*.jks`, `*.keystore`, `keystore.properties`, `*.apk`
  and `*.aab`.

## 2. Bump the version

In `app/build.gradle.kts`: increase `versionCode` by 1 for every release, and set `versionName`
(for example `1.0.1`). Add an entry to `CHANGELOG.md`.

## 3. Build the signed APK

*Build > Generate Signed App Bundle or APK > APK*, pick the keystore, choose the **release** variant and
tick both signature versions (V1 and V2) if offered. The result is in `app/release/app-release.apk`.

Rename it so the version is in the name:

```
ScreenOff-1.0.0.apk
```

Install it on a phone and check that the gestures work before publishing.

## 4. Checksum

```
# Windows
certutil -hashfile ScreenOff-1.0.0.apk SHA256
# macOS / Linux
shasum -a 256 ScreenOff-1.0.0.apk
```

Copy the hash into the release notes so users can verify their download.

## 5. Publish on GitHub

First time only, create the repository:

1. On GitHub: **New repository**, name it (for example `ScreenOff`), set it to **Public**, and do not
   add a README, licence or .gitignore (they already exist here).
2. In the project folder:

```
git init
git add .
git commit -m "Initial public release"
git branch -M main
git remote add origin https://github.com/shhhamik/ScreenOff.git
git push -u origin main
```

(Alternatively use *Add file > Upload files* on the GitHub page.)

Then, for each release:

```
git tag v1.0.0
git push origin v1.0.0
```

On GitHub: **Releases > Draft a new release**, choose the tag, paste the matching `CHANGELOG.md` entry,
add the SHA-256 line, attach `ScreenOff-1.0.0.apk`, and publish.

Before pushing, check that no keystore, `local.properties` or APK is staged: `git status`.

## Renaming the app or package

The package is `dev.shamik.screenoff`. To change it, replace every occurrence in:

- `app/build.gradle.kts` (`namespace` and `applicationId`)
- the `package` line of each Kotlin file, and the folder path under `app/src/main/java/`
- `app/src/main/AndroidManifest.xml` and `res/xml/accessibility_service_config.xml`
  (`settingsActivity`)

Android Studio's *Refactor > Rename* on the package does most of this. A different `applicationId` makes
a different app: it installs next to the old one and does not update it.

## Optional: tell friends how to update

Updates only install over the old version if the new APK is signed with the **same key**. Tell users to
download the new APK from Releases and open it; their settings are kept.
