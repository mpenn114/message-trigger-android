package uk.co.messagetrigger;
import android.app.Notification;
import android.os.Bundle;
import android.os.Build;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import java.util.List;
/** Reads newest visible message from a supported notification. */
public final class NotificationParser {
    /** Args: none. Returns: nothing. */
    private NotificationParser(){}
    /** Args: package name. Returns: platform or null. */
    public static String platformFor(String p) {
        if("com.whatsapp".equals(p)||"com.whatsapp.w4b".equals(p))return "WhatsApp";
        if("com.microsoft.teams".equals(p))return "Teams";
        switch(p){
            case "com.google.android.apps.messaging":
            case "com.samsung.android.messaging":
            case "com.android.mms":
            case "com.android.messaging":
            case "com.miui.mms":return "SMS";
            default:return null;
        }
    }
    /** Args: notification. Returns: parsed message, or null. */
    public static MessageData from(StatusBarNotification sbn){
        String platform=platformFor(sbn.getPackageName());if(platform==null)return null;
        Notification n=sbn.getNotification();
        if((n.flags&Notification.FLAG_GROUP_SUMMARY)!=0||n.extras==null)return null;
        Bundle e=n.extras;
        String title=str(e.getCharSequence(Notification.EXTRA_TITLE));
        String conversation=str(e.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE));
        if(conversation.isEmpty())conversation=title;
        String sender=title,body=str(e.getCharSequence(Notification.EXTRA_TEXT));
        long timestamp=sbn.getPostTime();
        Parcelable[] bundles=e.getParcelableArray(Notification.EXTRA_MESSAGES);
        if(bundles!=null&&bundles.length>0){
            List<Notification.MessagingStyle.Message> messages=Notification.MessagingStyle.Message.getMessagesFromBundleArray(bundles);
            if(!messages.isEmpty()){
                Notification.MessagingStyle.Message latest=messages.get(messages.size()-1);
                body=str(latest.getText());
                String actualSender=Build.VERSION.SDK_INT>=28&&latest.getSenderPerson()!=null?
                    str(latest.getSenderPerson().getName()):str(latest.getSender());
                if(!actualSender.isEmpty())sender=actualSender;
                if(latest.getTimestamp()>0)timestamp=latest.getTimestamp();
            }
        }
        if(body.trim().isEmpty())return null;
        return new MessageData(platform,sender,conversation,body,timestamp);
    }
    /** Args: optional text. Returns: non-null string. */
    private static String str(CharSequence s){return s==null?"":s.toString();}
}
