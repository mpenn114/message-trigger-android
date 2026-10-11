package uk.co.messagetrigger;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
/** Stores rules privately on-device. */
public final class RuleStore {
    private static final String PREFS="message_trigger_rules",KEY="rules";
    /** Args: none. Returns: nothing. */
    private RuleStore(){}
    /** Args: context. Returns: saved rules. */
    public static synchronized List<Rule> load(Context context) {
        List<Rule> result=new ArrayList<>();
        try {
            JSONArray array=new JSONArray(prefs(context).getString(KEY,"[]"));
            for(int i=0;i<array.length();i++){
                JSONObject obj=array.getJSONObject(i);
                result.add(new Rule(obj.optString("id"),obj.optString("platform"),
                    obj.optString("senderContains"),obj.optString("conversationContains"),
                    obj.optString("bodyContains"),obj.optBoolean("enabled",true)));
            }
        } catch(JSONException ignored) {}
        return result;
    }
    /** Args: context and rules. Returns: nothing. */
    public static synchronized void save(Context context,List<Rule> rules) {
        JSONArray array=new JSONArray();
        for(Rule rule:rules){
            JSONObject obj=new JSONObject();
            try {
                obj.put("id",rule.id);obj.put("platform",rule.platform);
                obj.put("senderContains",rule.senderContains);
                obj.put("conversationContains",rule.conversationContains);
                obj.put("bodyContains",rule.bodyContains);
                obj.put("enabled",rule.enabled);array.put(obj);
            } catch(JSONException e){throw new IllegalStateException(e);}
        }
        prefs(context).edit().putString(KEY,array.toString()).apply();
    }
    /** Args: context. Returns: private preferences. */
    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }
}
