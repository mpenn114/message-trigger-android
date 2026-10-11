package uk.co.messagetrigger;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Local persistence of pending alarms for reboot restoration. */
public final class ScheduledAlarmStore {
    private static final String PREFS = "pending_message_alarms", KEY = "pending";

    /** Args: none. Returns: nothing. */
    private ScheduledAlarmStore() {}

    /** Args: context. Returns: stored pending alarms. */
    public static synchronized List<PendingAlarm> load(Context context) {
        List<PendingAlarm> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs(context).getString(KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                result.add(new PendingAlarm(item.getString("id"), item.getString("platform"),
                    item.getLong("time")));
            }
        } catch (JSONException ignored) {}
        return result;
    }

    /** Args: context, rule ID, platform and timestamp. Returns: nothing. */
    public static synchronized void put(Context context, String id, String platform, long when) {
        List<PendingAlarm> list = load(context);
        list.removeIf(item -> item.id.equals(id));
        list.add(new PendingAlarm(id, platform, when));
        save(context, list);
    }

    /** Args: context and rule ID. Returns: nothing. */
    public static synchronized void remove(Context context, String id) {
        List<PendingAlarm> list = load(context);
        list.removeIf(item -> item.id.equals(id));
        save(context, list);
    }

    /** Args: context and pending alarms. Returns: nothing. */
    private static void save(Context context, List<PendingAlarm> list) {
        JSONArray array = new JSONArray();
        for (PendingAlarm item : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", item.id);
                obj.put("platform", item.platform);
                obj.put("time", item.when);
                array.put(obj);
            } catch (JSONException error) {
                throw new IllegalStateException(error);
            }
        }
        prefs(context).edit().putString(KEY, array.toString()).commit();
    }

    /** Args: context. Returns: the private preferences instance. */
    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Immutable pending alarm for disk storage. */
    public static final class PendingAlarm {
        public final String id;
        public final String platform;
        public final long when;

        /** Args: ID, platform and time. Returns: a pending alarm. */
        public PendingAlarm(String id, String platform, long when) {
            this.id = id;
            this.platform = platform;
            this.when = when;
        }
    }
}
