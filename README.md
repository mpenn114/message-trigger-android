# Message Trigger — Android

An experimental Android app that sounds a repeating alarm when an incoming **WhatsApp**, **Microsoft Teams**, or **SMS** notification matches a user-defined rule.

## Features
- Add and delete multiple rules.
- Select WhatsApp, Teams or SMS.
- Match sender, chat/conversation and message body using case-insensitive substrings. All non-empty predicates must match.
- Schedules a near-immediate alarm through Android AlarmManager.
- Repeating default alarm sound, foreground notification, lock-screen alarm activity and dismiss button.
- On-device storage of rules only, with no internet or account credentials.
- Test alarm button and permission status display.

## Build
Requires JDK 17 and Android SDK 35. Open the root directory in Android Studio or run `gradle testDebugUnitTest assembleDebug` using Gradle 8.11.1. The GitHub Actions workflow builds and uploads a debug APK under the **Actions** tab when CI succeeds. The Gradle wrapper is not included; Android Studio or a local Gradle installation is required.

## Setup
1. Install the debug build on an Android device (Android 8 or later).
2. Open Message Trigger and grant **notification access**.
3. Grant **Alarms & reminders / exact alarm** access (where available).
4. Grant **notification** permission on Android 13 and later.
5. Grant **full-screen intent** permission on Android 14 and later if available.
6. Ensure WhatsApp/Teams/SMS message notifications and preview text are enabled.
7. Create a sender, chat or text rule and try the test alarm.

## Limitations
- WhatsApp and Teams do not expose their inboxes through this app. This app can only inspect *Android notifications* delivered on the device. Muted chats, hidden previews, inaccessible work profiles and platform-specific notification formats can prevent matching.
- SMS is also sourced from messaging-app notifications, not direct SMS_RECEIVED access; the package name must be supported by `NotificationParser`.
- Grouped notifications, editing, replay and repeated posts may behave differently across device brands.
- Alarm reliability depends on Android permissions, battery policies and foreground service restrictions; this is **not** yet verified on physical devices.
- This is a prototype, not an independently reviewed, production-ready alarm application. Do not depend on it for emergencies or critical alerts.
- This code has not been compiled or device-tested in this environment.

## How it works
`IncomingNotificationService` receives notifications, `NotificationParser` extracts visible message data, `RuleMatcher` evaluates the locally stored filters, `AlarmScheduler` schedules the alert, and `RingingService` repeats the default alarm sound until dismissed. No message body is persisted.

## Licence
MIT — see LICENSE.
