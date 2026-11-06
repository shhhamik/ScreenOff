# Security Policy

Screen Off is an accessibility service, so its security matters. It is designed to have the smallest
access that still works: no internet permission, `canRetrieveWindowContent="false"`, and no stored data
except the user's own settings.

## Supported versions

Only the latest release receives fixes.

## Reporting a vulnerability

Please **do not** open a public issue for a security problem.

Use GitHub's private reporting instead: open the **Security** tab of this repository and choose
**Report a vulnerability**. Include what you found, the steps to reproduce it, and the affected version.

You can expect an acknowledgement within a few days. This is a spare-time project, so fixes are made on
a best-effort basis.

## Verifying a download

Release notes list the SHA-256 checksum of each APK. Only install APKs from this repository's Releases
page, and compare the checksum before installing. See the README for the commands.
