package uk.co.messagetrigger;
import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/** Rule editor and permission setup screen. */
public final class MainActivity extends Activity {
    private static final String[] PLATFORMS={"WhatsApp","Teams","SMS"};
    private LinearLayout root,rulesContainer;
    private TextView accessStatus;
    private EditText senderField,conversationField,textField;
    private Spinner platformField;
    /** Args: state. Returns: nothing. */
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);NotificationHelper.createChannels(this);
        ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);
        root.setPadding(dp(20),dp(24),dp(20),dp(28));root.setOrientation(1);
        scroll.addView(root);setContentView(scroll);
        heading("Message Trigger",27);
        paragraph("Raise a repeating alarm when a WhatsApp, Teams or SMS notification matches your rules. Everything stays on this phone.");
        heading("1. Enable access",20);accessStatus=paragraph("");
        action("Allow notification access",()->startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        action("Allow exact alarms",()->{
            if(Build.VERSION.SDK_INT>=31){
                try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).setData(Uri.parse("package:"+getPackageName())));}
                catch(Exception ignored){toast("Open Alarms & reminders in Android settings");}
            }else toast("Already supported on this Android version");
        });
        action("Allow alarm notifications",()->{
            if(Build.VERSION.SDK_INT>=33)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},100);
            else toast("Already supported on this Android version");
        });
        action("Allow full-screen alarm display",()->{
            if(Build.VERSION.SDK_INT>=34){
                try{startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).setData(Uri.parse("package:"+getPackageName())));}
                catch(Exception ignored){toast("Open Full-screen intents in Android settings");}
            }else toast("Already supported on this Android version");
        });
        paragraph("Enable permissions and ensure message previews appear in notifications. Hidden content cannot match text filters.");
        heading("2. Create a rule",20);
        paragraph("Every filled field must match (case-insensitive substring). Blank fields match any value.");
        platformField=new Spinner(this);
        platformField.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,PLATFORMS));
        root.addView(platformField);
        senderField=field("Sender contains (optional)");
        conversationField=field("Chat / conversation contains (optional)");
        textField=field("Message text contains (optional)");
        action("Add rule",this::addRule);
        heading("3. Current rules",20);
        rulesContainer=new LinearLayout(this);rulesContainer.setOrientation(1);root.addView(rulesContainer);
        heading("4. Test",20);
        paragraph("Schedules a test alarm for about two seconds from now.");
        action("Test alarm",()->AlarmScheduler.schedule(this,"test"));
        renderRules();
    }
    /** Args: none. Returns: nothing; refreshes permissions. */
    @Override protected void onResume(){
        super.onResume();if(accessStatus==null)return;
        boolean listener=isListenerEnabled(),exact=AlarmScheduler.isAllowed(this);
        boolean notifications=Build.VERSION.SDK_INT<33||checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        boolean fullScreen=Build.VERSION.SDK_INT<34||nm.canUseFullScreenIntent();
        accessStatus.setText("Notification access: "+yesNo(listener)+"\nExact alarms: "+yesNo(exact)+
            "\nAlarm notifications: "+yesNo(notifications)+"\nFull-screen alarms: "+yesNo(fullScreen));
    }
    /** Args: none. Returns: true if listener enabled. */
    private boolean isListenerEnabled(){
        String enabled=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
        ComponentName component=new ComponentName(this,IncomingNotificationService.class);
        if(enabled==null)return false;
        for(String name:enabled.split(":"))if(component.equals(ComponentName.unflattenFromString(name)))return true;
        return false;
    }
    /** Args: granted. Returns: readable status. */
    private String yesNo(boolean granted){return granted?"enabled":"NOT ENABLED";}
    /** Args: none. Returns: nothing; adds a rule. */
    private void addRule(){
        String sender=senderField.getText().toString().trim(),chat=conversationField.getText().toString().trim(),
            body=textField.getText().toString().trim();
        if(sender.isEmpty()&&chat.isEmpty()&&body.isEmpty()){toast("Enter at least one filter");return;}
        List<Rule> rules=RuleStore.load(this);
        rules.add(new Rule(UUID.randomUUID().toString(),PLATFORMS[platformField.getSelectedItemPosition()],sender,chat,body,true));
        RuleStore.save(this,rules);senderField.setText("");conversationField.setText("");textField.setText("");
        renderRules();toast("Rule added");
    }
    /** Args: none. Returns: nothing; renders stored rules. */
    private void renderRules(){
        rulesContainer.removeAllViews();
        List<Rule> rules=RuleStore.load(this);
        if(rules.isEmpty()){TextView empty=new TextView(this);empty.setText("No rules yet.");rulesContainer.addView(empty);}
        for(Rule rule:rules){
            LinearLayout item=new LinearLayout(this);item.setPadding(0,dp(8),0,dp(8));item.setOrientation(1);
            TextView summary=new TextView(this);summary.setTextSize(16);
            summary.setText(rule.platform+"\nSender: "+any(rule.senderContains)+"\nChat: "+any(rule.conversationContains)+"\nText: "+any(rule.bodyContains));
            item.addView(summary);Button delete=new Button(this);delete.setText("Delete this rule");
            delete.setOnClickListener(view->removeRule(rule.id));item.addView(delete);rulesContainer.addView(item);
        }
    }
    /** Args: ID. Returns: nothing; deletes matching rule. */
    private void removeRule(String id){
        List<Rule> rules=new ArrayList<>(RuleStore.load(this));rules.removeIf(rule->rule.id.equals(id));
        RuleStore.save(this,rules);renderRules();
    }
    /** Args: value. Returns: visual wildcard if empty. */
    private String any(String value){return value.isEmpty()?"(any)":value;}
    /** Args: text and sp. Returns: nothing. */
    private void heading(String value,int size){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setPadding(0,dp(14),0,dp(7));root.addView(t);
    }
    /** Args: text. Returns: label. */
    private TextView paragraph(String value){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(14);t.setPadding(0,0,0,dp(12));root.addView(t);return t;
    }
    /** Args: hint. Returns: edit field. */
    private EditText field(String hint){EditText t=new EditText(this);t.setSingleLine(true);t.setHint(hint);root.addView(t);return t;}
    /** Args: label and callback. Returns: nothing. */
    private void action(String label,Runnable click){Button b=new Button(this);b.setText(label);b.setOnClickListener(v->click.run());root.addView(b);}
    /** Args: dp. Returns: pixels. */
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    /** Args: message. Returns: nothing. */
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_SHORT).show();}
}
