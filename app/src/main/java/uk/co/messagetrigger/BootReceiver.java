package uk.co.messagetrigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Recreates alarms after a restart or application upgrade. */
public final class BootReceiver extends BroadcastReceiver {
    /** Args: receiver context and system broadcast. Returns: nothing. */
    @Override public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) return;
        long now = System.currentTimeMillis();
        for (ScheduledAlarmStore.PendingAlarm alarm : ScheduledAlarmStore.load(context)) {
            AlarmScheduler.scheduleAt(context, alarm.id, alarm.platform, Math.max(alarm.when, now + 1500L));
        }
    }
}
