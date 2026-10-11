# Message Trigger — Android

An experimental Android app that sounds a repeating alarm when an incoming **WhatsApp**, **Microsoft Teams**, or **SMS** notification matches a user-defined rule.

## Features
- Create/delete multiple independent rules with **case-insensitive sender, chat and message-text filters**.
- Choose **Ring immediately** or **At a set time** (24-hour clock, e.g. 06:00).
- Scheduled alarms trigger at the *next occurrence* of the chosen **local time**: today if still upcoming, tomorrow otherwise. Each trigger schedules a one-off alarm; this is not a daily repeating alarm.
- A modern, dark-navy/indigo dashboard with status badges, rule cards and a time picker.
- Independently track each rule's pending alarm; if a rule matches again, its next alarm replaces the previous alarm for that rule. Different rules do not overwrite one another.
- Preserve older rules (which remain immediate) across upgrades.
- Restore pending alarms after device reboot or app update. If the original time has already passed during the reboot, the saved alarm triggers soon after startup rather than being silently lost.
- Repeating default alarm sound, foreground notification, lock-screen alarm activity and dismiss button.
- On-device storage of rules and scheduled alarm metadata only; no internet or account credentials.
- Test alarm button and permission indicators.

## Build
Requires JDK 17 and Android SDK 35. Open the root directory in Android Studio or run `gradle testDebugUnitTest assembleDebug` using Gradle 8.11.1. GitHub Actions builds and uploads the APK under the **Actions** tab. The Gradle wrapper is not included; Android Studio or a local Gradle installation is required.

## Setup
1. Install the debug build on an Android device (Android 8 or later).
2. Open Message Trigger and grant **notification access**. Sideloaded APKs may require Settings → Apps → Message Trigger → three-dot menu → Allow restricted settings first.
3. Grant **Alarms & reminders / exact alarm** access where available.
4. Grant **notification** permission on Android 13 and later.
5. Grant **full-screen intent** permission on Android 14 and later if available.
6. Ensure WhatsApp/Teams/SMS message notifications and preview text are enabled.
7. Create a trigger and use the test alarm.

### Example: a 6 am wake-up alarm from Grace
Set **Platform** to WhatsApp, **Sender contains** to `Grace`, **Message contains** to `alarm`, select **At a set time**, and choose **06:00**. When the message arrives, the phone schedules one alarm for the next 06:00 in its local time zone. If two different matching rules exist, each is scheduled separately.

## Limitations
- WhatsApp and Teams don't expose their inboxes through this app. Only Android notifications delivered to the phone are inspected. Hidden previews, muted chats, inaccessible work profiles or unfamiliar notification formats can prevent matching.
- SMS comes from messaging-app notifications rather than direct SMS access; the package must be recognised by `NotificationParser`.
- Grouped notifications, edits, repeats, Android alarm permissions, power-saving settings and lock-screen behaviour vary by phone.
- Existing app permissions should normally survive an in-place update when using the same debug signing key; build runners may use different debug signing keys, so you might need to uninstall/reinstall between debug builds (which loses saved rules).
- This is a prototype, not a production-critical alarm application. Do not rely on it for emergencies.
- Source changes are tested by GitHub Actions, but real-device reliability still needs testing.

## Technical overview
`IncomingNotificationService` receives notifications, `NotificationParser` extracts visible message data, `RuleMatcher` evaluates rules, `AlarmTime` calculates the next local-time occurrence, and `AlarmScheduler` places one Android alarm per matching rule. Pending alarm metadata is stored by `ScheduledAlarmStore` so `BootReceiver` can restore it after reboot. `RingingService` plays the default alarm sound until dismissed. Message bodies are **not** persisted or uploaded.

## Licence
MIT — see LICENSE.
