package uk.co.messagetrigger;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
/** Builds alarm notifications and permission warnings. */
public final class NotificationHelper{
    public static final String RING_CHANNEL="ringing_alarm_v1",WARNING_CHANNEL="permission_warning_v1";
    public static final int RING_ID=4402;
    private static final int WARNING_ID=4403;
    /** Args: none. Returns: nothing. */
    private NotificationHelper(){}
    /** Args: context. Returns: nothing; creates channels. */
    public static void createChannels(Context context){
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel ring=new NotificationChannel(RING_CHANNEL,"Triggered alarms",NotificationManager.IMPORTANCE_HIGH);
        ring.setDescription("Ringing alerts for user-defined incoming message rules");
        ring.setSound(null,null);ring.enableVibration(true);ring.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(ring);
        NotificationChannel warning=new NotificationChannel(WARNING_CHANNEL,"Permission problems",NotificationManager.IMPORTANCE_HIGH);
        warning.setDescription("Cannot ring without Android access");
        manager.createNotificationChannel(warning);
    }
    /** Args: context and platform. Returns: ringing notification. */
    public static Notification ringing(Context context,String platform){
        Intent view=new Intent(context,AlarmActivity.class).putExtra("platform",platform);
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent open=PendingIntent.getActivity(context,4401,view,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent dismiss=PendingIntent.getService(context,4402,
            new Intent(context,RingingService.class).setAction(RingingService.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(context,RING_CHANNEL)
            .setSmallIcon(R.drawable.ic_alarm).setContentTitle("Message Trigger alarm")
            .setContentText("A "+platform+" message matched a rule")
            .setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PUBLIC)
            .setPriority(Notification.PRIORITY_MAX).setOngoing(true)
            .setContentIntent(open).setFullScreenIntent(open,true)
            .addAction(new Notification.Action.Builder(R.drawable.ic_alarm,"Dismiss",dismiss).build()).build();
    }
    /** Args: context. Returns: nothing; posts alarm permission warning. */
    public static void showPermissionFallback(Context context){
        createChannels(context);
        PendingIntent open=PendingIntent.getActivity(context,4403,new Intent(context,MainActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification n=new Notification.Builder(context,WARNING_CHANNEL)
            .setSmallIcon(R.drawable.ic_alarm).setContentTitle("Message matched, but alarm unavailable")
            .setContentText("Open Message Trigger and enable its alarm permissions")
            .setAutoCancel(true).setContentIntent(open).build();
        try{((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(WARNING_ID,n);}
        catch(SecurityException ignored){}
    }
}
