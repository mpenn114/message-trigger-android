package uk.co.messagetrigger;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
/** Schedules a near-immediate alarm. */
public final class AlarmScheduler {
    private static final int REQUEST=4501;
    /** Args: none. Returns: nothing. */
    private AlarmScheduler(){}
    /** Args: context. Returns: whether exact alarms are allowed. */
    public static boolean isAllowed(Context context) {
        AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        return Build.VERSION.SDK_INT<31 || mgr.canScheduleExactAlarms();
    }
    /** Args: context and platform. Returns: nothing. */
    public static void schedule(Context context,String platform) {
        NotificationHelper.createChannels(context);
        if(!isAllowed(context)) {NotificationHelper.showPermissionFallback(context);return;}
        Intent receiverIntent=new Intent(context,AlarmReceiver.class).putExtra("platform",platform);
        PendingIntent receiver=PendingIntent.getBroadcast(context,REQUEST,receiverIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent show=PendingIntent.getActivity(context,REQUEST,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        try {mgr.setAlarmClock(new AlarmManager.AlarmClockInfo(System.currentTimeMillis()+1500L,show),receiver);}
        catch(SecurityException e){NotificationHelper.showPermissionFallback(context);}
    }
}
