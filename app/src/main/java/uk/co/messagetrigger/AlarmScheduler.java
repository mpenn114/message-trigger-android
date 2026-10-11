package uk.co.messagetrigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import java.time.Instant;
import java.time.ZoneId;

/** Schedules per-rule alarms with unique PendingIntents. */
public final class AlarmScheduler {
    private static final String TEST_ID = "manual-test";

    /** Args: none. Returns: nothing. */
    private AlarmScheduler() {}

    /** Args: context. Returns: whether exact alarms are allowed by Android. */
    public static boolean isAllowed(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms();
    }

    /** Args: context and matching rule. Returns: scheduled epoch milliseconds, or -1 if unavailable. */
    public static long schedule(Context context, Rule rule) {
        long now = System.currentTimeMillis();
        long when = rule.alarmTime.isEmpty() ? now + 1500L
            : AlarmTime.nextOccurrence(rule.alarmTime, Instant.ofEpochMilli(now), ZoneId.systemDefault());
        return scheduleAt(context, rule.id, rule.platform, when);
    }

    /** Args: context. Returns: scheduled time, or -1 on failure. */
    public static long test(Context context) {
        return scheduleAt(context, TEST_ID, "test", System.currentTimeMillis() + 1500L);
    }

    /** Args: context, rule ID, platform and absolute fire time. Returns: scheduled time, or -1. */
    public static long scheduleAt(Context context, String id, String platform, long when) {
        NotificationHelper.createChannels(context);
        if (!isAllowed(context)) {
            NotificationHelper.showPermissionFallback(context);
            return -1;
        }
        Intent broadcast = new Intent(context, AlarmReceiver.class)
            .setData(Uri.parse("messagetrigger://alarm/" + Uri.encode(id)))
            .putExtra("platform", platform)
            .putExtra("alarmId", id);
        PendingIntent receiver = PendingIntent.getBroadcast(context, 4501, broadcast,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent show = PendingIntent.getActivity(context, 4501,
            new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try {
            manager.setAlarmClock(new AlarmManager.AlarmClockInfo(when, show), receiver);
            ScheduledAlarmStore.put(context, id, platform, when);
            return when;
        } catch (SecurityException e) {
            NotificationHelper.showPermissionFallback(context);
            return -1;
        }
    }

    /** Args: context and ID. Returns: nothing; cancels an outstanding alarm for that rule. */
    public static void cancel(Context context, String id) {
        Intent broadcast = new Intent(context, AlarmReceiver.class)
            .setData(Uri.parse("messagetrigger://alarm/" + Uri.encode(id)));
        PendingIntent receiver = PendingIntent.getBroadcast(context, 4501, broadcast,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (receiver != null) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(receiver);
            receiver.cancel();
        }
        ScheduledAlarmStore.remove(context, id);
    }
}
