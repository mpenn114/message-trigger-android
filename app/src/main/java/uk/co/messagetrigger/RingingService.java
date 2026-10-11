package uk.co.messagetrigger;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
/** Foreground service playing repeating alarm audio until dismissed. */
public final class RingingService extends Service {
    public static final String ACTION_START="uk.co.messagetrigger.START",ACTION_STOP="uk.co.messagetrigger.STOP";
    private MediaPlayer player;
    /** Args: intent, flags, start ID. Returns: service restart policy. */
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null&&ACTION_STOP.equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(player!=null)return START_NOT_STICKY;
        String platform=intent==null?"message":intent.getStringExtra("platform");
        if(platform==null)platform="message";
        NotificationHelper.createChannels(this);
        startForeground(NotificationHelper.RING_ID,NotificationHelper.ringing(this,platform));
        playAlarm();return START_NOT_STICKY;
    }
    /** Args: none. Returns: nothing; plays looping alarm sound. */
    private void playAlarm(){
        Uri alarm=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if(alarm==null)alarm=Settings.System.DEFAULT_NOTIFICATION_URI;
        MediaPlayer candidate=null;
        try{
            candidate=new MediaPlayer();
            candidate.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            candidate.setDataSource(this,alarm);candidate.setLooping(true);candidate.prepare();candidate.start();player=candidate;
        }catch(Exception e){if(candidate!=null)candidate.release();}
    }
    /** Args: none. Returns: nothing; releases audio and notification. */
    @Override public void onDestroy(){
        if(player!=null){player.stop();player.release();player=null;}
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(NotificationHelper.RING_ID);
        super.onDestroy();
    }
    /** Args: intent. Returns: null (not bound). */
    @Override public IBinder onBind(Intent intent){return null;}
}
