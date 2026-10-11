package uk.co.messagetrigger;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
/** Processes notifications entirely on the device. */
public final class IncomingNotificationService extends NotificationListenerService {
    private static final long DEDUPE_MILLIS=90000L;
    private final Map<String,Long> recent=new LinkedHashMap<>();
    /** Args: posted notification. Returns: nothing. */
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        MessageData message=NotificationParser.from(sbn);
        if(message==null || RuleMatcher.firstMatch(RuleStore.load(this),message)==null)return;
        long now=System.currentTimeMillis();
        String fingerprint=sbn.getKey()+"|"+message.sender+"|"+message.conversation+"|"+message.body+"|"+message.timestamp;
        synchronized(recent){
            Iterator<Map.Entry<String,Long>> iterator=recent.entrySet().iterator();
            while(iterator.hasNext()) if(now-iterator.next().getValue()>DEDUPE_MILLIS)iterator.remove();
            if(recent.containsKey(fingerprint))return;
            recent.put(fingerprint,now);
            while(recent.size()>200)recent.remove(recent.keySet().iterator().next());
        }
        AlarmScheduler.schedule(this,message.platform);
    }
}
