package uk.co.messagetrigger;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
/** Lock-screen alarm UI, with no message contents. */
public final class AlarmActivity extends Activity {
    /** Args: saved state. Returns: nothing. */
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);setShowWhenLocked(true);setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LinearLayout root=new LinearLayout(this);root.setPadding(32,64,32,32);root.setOrientation(1);root.setGravity(Gravity.CENTER);
        TextView title=new TextView(this);title.setTextSize(32);title.setText("Message Trigger alarm");root.addView(title);
        TextView info=new TextView(this);String platform=getIntent().getStringExtra("platform");
        info.setText("A "+(platform==null?"new":platform)+" message matched your rule.");info.setTextSize(18);root.addView(info);
        Button button=new Button(this);button.setText("Dismiss alarm");button.setOnClickListener(v->dismissAlarm());root.addView(button);setContentView(root);
    }
    /** Args: none. Returns: nothing. */
    private void dismissAlarm(){stopService(new Intent(this,RingingService.class));finish();}
}
