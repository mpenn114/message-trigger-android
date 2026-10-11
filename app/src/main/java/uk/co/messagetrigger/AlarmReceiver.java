package uk.co.messagetrigger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
/** Alarm delivery receiver. */
public final class AlarmReceiver extends BroadcastReceiver {
    /** Args: context and intent. Returns: nothing. */
    @Override public void onReceive(Context context, Intent intent) {
        String platform = intent.getStringExtra("platform");
        Intent service = new Intent(context,RingingService.class);
        service.setAction(RingingService.ACTION_START);
        service.putExtra("platform",platform == null ? "message" : platform);
        try { if(Build.VERSION.SDK_INT>=26) context.startForegroundService(service); else context.startService(service); }
        catch(RuntimeException e) { NotificationHelper.showPermissionFallback(context); }
    }
}
