package uk.co.messagetrigger;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
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

/** Polished single-screen rule editor and notification/alarm permission dashboard. */
public final class MainActivity extends Activity {
    private static final String[] PLATFORMS = {"WhatsApp", "Teams", "SMS"};
    private static final int INK = Color.rgb(12, 25, 56);
    private static final int MUTED = Color.rgb(99, 112, 139);
    private static final int ACCENT = Color.rgb(64, 76, 213);
    private static final int GREEN = Color.rgb(0, 154, 124);
    private static final int SURFACE = Color.rgb(245, 247, 252);
    private static final int BORDER = Color.rgb(227, 232, 242);

    private LinearLayout root;
    private LinearLayout permissionContainer;
    private LinearLayout rulesContainer;
    private TextView ruleCount;
    private EditText senderField;
    private EditText conversationField;
    private EditText textField;
    private Spinner platformField;
    private Button immediateButton;
    private Button scheduledButton;
    private Button timeButton;
    private TextView timeHelper;
    private boolean scheduledMode = false;
    private String selectedTime = "06:00";

    /** Args: saved instance state. Returns: nothing; displays the main dashboard. */
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(8, 22, 50));
        getWindow().setNavigationBarColor(SURFACE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        NotificationHelper.createChannels(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(SURFACE);
        root = new LinearLayout(this);
        root.setPadding(dp(18), dp(20), dp(18), dp(32));
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);
        setContentView(scroll);

        makeHero();
        makePermissions();
        makeComposer();
        makeRuleList();
        makeTest();
        renderRules();
        refreshPermissions();
    }

    /** Args: none. Returns: nothing; refreshes access indicators on return from settings. */
    @Override protected void onResume() {
        super.onResume();
        if (permissionContainer != null) refreshPermissions();
    }

    /** Args: none. Returns: nothing; builds the header. */
    private void makeHero() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(22), dp(24), dp(22), dp(25));
        GradientDrawable background = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(9, 24, 66), Color.rgb(56, 64, 148)});
        background.setCornerRadius(dp(28));
        header.setBackground(background);
        header.setElevation(dp(4));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.bottomMargin = dp(19);
        root.addView(header, hp);

        TextView eyebrow = label("●  MESSAGE AUTOMATION", 11, Color.rgb(130, 232, 208), true);
        eyebrow.setLetterSpacing(0.14f);
        header.addView(eyebrow);
        TextView title = label("Never miss what\nmatters.", 31, Color.WHITE, true);
        title.setLineSpacing(dp(3), 1.0f);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.topMargin = dp(12);
        header.addView(title, tp);
        TextView subtitle = label("Turn messages into alarms — instantly or at a time you choose.", 14,
            Color.rgb(209, 221, 244), false);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(10);
        header.addView(subtitle, sp);
        TextView tags = label("WhatsApp     •     Teams     •     SMS", 12, Color.WHITE, true);
        tags.setPadding(dp(13), dp(10), dp(13), dp(10));
        tags.setBackground(shape(Color.argb(42, 255, 255, 255), 12, 0, 0));
        LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(-1, -2);
        tagParams.topMargin = dp(20);
        header.addView(tags, tagParams);
    }

    /** Args: none. Returns: nothing; builds the access checklist. */
    private void makePermissions() {
        LinearLayout card = card();
        sectionHeading(card, "01", "Ready to listen?", "Enable these permissions once.");
        permissionContainer = column();
        card.addView(permissionContainer);
        TextView footnote = label("Message previews must be visible in your phone's notifications.", 12, MUTED, false);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = dp(12);
        card.addView(footnote, fp);
    }

    /** Args: none. Returns: nothing; redraws settings with current Android permission state. */
    private void refreshPermissions() {
        permissionContainer.removeAllViews();
        boolean listener = isListenerEnabled();
        boolean exact = AlarmScheduler.isAllowed(this);
        boolean notifications = Build.VERSION.SDK_INT < 33
            || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        boolean fullScreen = Build.VERSION.SDK_INT < 34 || manager.canUseFullScreenIntent();
        permissionRow("Notification access", "Read incoming message notifications", listener, () ->
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        permissionRow("Exact alarms", "Schedule alarms for precise times", exact, () -> {
            if (Build.VERSION.SDK_INT >= 31) {
                try {
                    startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData(Uri.parse("package:" + getPackageName())));
                } catch (Exception ignored) {
                    toast("Open Alarms & reminders in Android settings");
                }
            }
        });
        permissionRow("Notifications", "Display alerts on your phone", notifications, () -> {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        });
        permissionRow("Full-screen alarm", "Show an alarm over the lock screen", fullScreen, () -> {
            if (Build.VERSION.SDK_INT >= 34) {
                try {
                    startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                        .setData(Uri.parse("package:" + getPackageName())));
                } catch (Exception ignored) {
                    toast("Open Full-screen intents in Android settings");
                }
            }
        });
    }

    /** Args: title, description, permission granted and action. Returns: nothing. */
    private void permissionRow(String title, String detail, boolean granted, Runnable onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        LinearLayout text = column();
        TextView name = label(title, 14, INK, true);
        TextView description = label(detail, 12, MUTED, false);
        text.addView(name);
        text.addView(description);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        TextView state = label(granted ? "✓  On" : "Enable  →", 12,
            granted ? GREEN : ACCENT, true);
        state.setPadding(dp(11), dp(8), dp(11), dp(8));
        state.setBackground(shape(granted ? Color.rgb(229, 248, 242) : Color.rgb(236, 239, 255),
            15, 0, 0));
        row.addView(state);
        if (!granted) row.setOnClickListener(v -> onClick.run());
        permissionContainer.addView(row);
    }

    /** Args: none. Returns: nothing; builds the rule creation form. */
    private void makeComposer() {
        LinearLayout card = card();
        sectionHeading(card, "02", "Create a trigger", "All the filled-in conditions must match.");
        inputLabel(card, "PLATFORM");
        platformField = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, PLATFORMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        platformField.setAdapter(adapter);
        platformField.setPadding(dp(12), dp(6), dp(12), dp(6));
        LinearLayout platformSurface = column();
        platformSurface.setBackground(shape(Color.rgb(247, 249, 253), 14, BORDER, 1));
        platformSurface.addView(platformField);
        card.addView(platformSurface, spaced(0, 5, 12));

        senderField = input(card, "SENDER CONTAINS", "e.g. Grace");
        conversationField = input(card, "CONVERSATION CONTAINS", "Optional chat or group name");
        textField = input(card, "MESSAGE CONTAINS", "e.g. alarm");

        inputLabel(card, "WHEN SHOULD IT RING?");
        LinearLayout modes = new LinearLayout(this);
        modes.setOrientation(LinearLayout.HORIZONTAL);
        immediateButton = button("⚡  Immediately", false);
        scheduledButton = button("◷  At a set time", false);
        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, dp(48), 1);
        left.rightMargin = dp(7);
        modes.addView(immediateButton, left);
        modes.addView(scheduledButton, new LinearLayout.LayoutParams(0, dp(48), 1));
        card.addView(modes);
        immediateButton.setOnClickListener(v -> selectMode(false));
        scheduledButton.setOnClickListener(v -> selectMode(true));

        timeButton = button("06:00   •   Change time", false);
        timeButton.setOnClickListener(v -> chooseTime());
        card.addView(timeButton, spaced(0, 12, 0));
        timeHelper = label("If that time has passed, the alarm will ring tomorrow.", 12, MUTED, false);
        card.addView(timeHelper, spaced(0, 6, 2));
        selectMode(false);

        Button save = button("+  Create trigger", true);
        save.setOnClickListener(v -> addRule());
        card.addView(save, spaced(0, 20, 0));
    }

    /** Args: whether to schedule for a clock time. Returns: nothing. */
    private void selectMode(boolean scheduled) {
        scheduledMode = scheduled;
        setSelectionStyle(immediateButton, !scheduled);
        setSelectionStyle(scheduledButton, scheduled);
        timeButton.setVisibility(scheduled ? View.VISIBLE : View.GONE);
        timeHelper.setVisibility(scheduled ? View.VISIBLE : View.GONE);
    }

    /** Args: target button and selected state. Returns: nothing. */
    private void setSelectionStyle(Button button, boolean selected) {
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(selected ? Color.WHITE : MUTED);
        button.setBackground(shape(selected ? ACCENT : Color.rgb(240, 243, 250), 14,
            selected ? ACCENT : BORDER, 1));
        button.setElevation(0);
    }

    /** Args: none. Returns: nothing; opens a 24-hour time picker. */
    private void chooseTime() {
        String[] parts = selectedTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        new TimePickerDialog(this, (view, h, m) -> {
            selectedTime = AlarmTime.format(h, m);
            timeButton.setText(selectedTime + "   •   Change time");
        }, hour, minute, true).show();
    }

    /** Args: none. Returns: nothing; adds a rule and clears its inputs. */
    private void addRule() {
        String sender = senderField.getText().toString().trim();
        String chat = conversationField.getText().toString().trim();
        String body = textField.getText().toString().trim();
        if (sender.isEmpty() && chat.isEmpty() && body.isEmpty()) {
            toast("Enter at least one matching condition");
            return;
        }
        List<Rule> rules = RuleStore.load(this);
        rules.add(new Rule(UUID.randomUUID().toString(), PLATFORMS[platformField.getSelectedItemPosition()],
            sender, chat, body, true, scheduledMode ? selectedTime : ""));
        RuleStore.save(this, rules);
        senderField.setText("");
        conversationField.setText("");
        textField.setText("");
        renderRules();
        toast(scheduledMode ? "Trigger created — alarm at " + selectedTime : "Trigger created");
    }

    /** Args: none. Returns: nothing; creates the saved-rules panel. */
    private void makeRuleList() {
        LinearLayout card = card();
        sectionHeading(card, "03", "Your triggers", "Rules are stored only on this device.");
        ruleCount = label("", 12, ACCENT, true);
        card.addView(ruleCount, spaced(0, 0, 9));
        rulesContainer = column();
        card.addView(rulesContainer);
    }

    /** Args: none. Returns: nothing; redraws stored rules. */
    private void renderRules() {
        if (rulesContainer == null) return;
        rulesContainer.removeAllViews();
        List<Rule> rules = RuleStore.load(this);
        ruleCount.setText(rules.size() + (rules.size() == 1 ? " active rule" : " active rules"));
        if (rules.isEmpty()) {
            TextView empty = label("No triggers yet. Create your first one above.", 13, MUTED, false);
            empty.setPadding(dp(15), dp(19), dp(15), dp(19));
            empty.setBackground(shape(Color.rgb(247, 249, 253), 14, BORDER, 1));
            rulesContainer.addView(empty);
            return;
        }
        for (Rule rule : rules) {
            LinearLayout panel = column();
            panel.setPadding(dp(15), dp(15), dp(15), dp(12));
            panel.setBackground(shape(Color.rgb(247, 249, 253), 16, BORDER, 1));
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2);
            pp.bottomMargin = dp(10);
            rulesContainer.addView(panel, pp);

            LinearLayout heading = new LinearLayout(this);
            heading.setGravity(Gravity.CENTER_VERTICAL);
            TextView platform = label(rule.platform, 13,
                rule.platform.equals("WhatsApp") ? GREEN : ACCENT, true);
            heading.addView(platform, new LinearLayout.LayoutParams(0, -2, 1));
            TextView timing = label(rule.alarmTime.isEmpty() ? "⚡ Immediate" : "◷  " + rule.alarmTime,
                12, rule.alarmTime.isEmpty() ? INK : ACCENT, true);
            timing.setPadding(dp(9), dp(6), dp(9), dp(6));
            timing.setBackground(shape(Color.rgb(233, 238, 251), 12, 0, 0));
            heading.addView(timing);
            panel.addView(heading);

            String filters = (rule.senderContains.isEmpty() ? "" : "From  " + rule.senderContains + "\n")
                + (rule.conversationContains.isEmpty() ? "" : "Chat  " + rule.conversationContains + "\n")
                + (rule.bodyContains.isEmpty() ? "" : "Message  “" + rule.bodyContains + "”");
            TextView summary = label(filters, 13, INK, false);
            summary.setLineSpacing(dp(3), 1.0f);
            panel.addView(summary, spaced(0, 9, 4));

            TextView remove = label("Remove trigger  ×", 12, Color.rgb(190, 72, 91), true);
            remove.setPadding(0, dp(11), 0, dp(2));
            remove.setOnClickListener(v -> removeRule(rule.id));
            panel.addView(remove);
        }
    }

    /** Args: rule ID. Returns: nothing; removes rule and cancels any pending alarm. */
    private void removeRule(String id) {
        AlarmScheduler.cancel(this, id);
        List<Rule> rules = new ArrayList<>(RuleStore.load(this));
        rules.removeIf(rule -> rule.id.equals(id));
        RuleStore.save(this, rules);
        renderRules();
        toast("Trigger removed");
    }

    /** Args: none. Returns: nothing; adds a small manual test panel. */
    private void makeTest() {
        LinearLayout card = card();
        sectionHeading(card, "04", "Test your alarm", "Check the sound and lock-screen alert.");
        Button test = button("▶  Ring a test alarm", false);
        setSelectionStyle(test, false);
        test.setOnClickListener(v -> {
            if (AlarmScheduler.test(this) >= 0) toast("Test alarm in about two seconds");
            else toast("Please enable exact alarms first");
        });
        card.addView(test, spaced(0, 7, 0));
        TextView privacy = label("PRIVATE BY DESIGN   •   NO MESSAGE DATA UPLOADED", 11, MUTED, true);
        privacy.setGravity(Gravity.CENTER);
        root.addView(privacy, spaced(0, 7, 0));
    }

    /** Args: none. Returns: new white card attached to the root. */
    private LinearLayout card() {
        LinearLayout panel = column();
        panel.setPadding(dp(18), dp(19), dp(18), dp(19));
        panel.setBackground(shape(Color.WHITE, 22, BORDER, 1));
        panel.setElevation(dp(2));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(16);
        root.addView(panel, p);
        return panel;
    }

    /** Args: card, section number, title and description. Returns: nothing. */
    private void sectionHeading(LinearLayout card, String number, String title, String description) {
        TextView label = label("STEP " + number, 11, ACCENT, true);
        label.setLetterSpacing(0.11f);
        card.addView(label);
        card.addView(label(title, 21, INK, true), spaced(0, 6, 4));
        card.addView(label(description, 13, MUTED, false), spaced(0, 0, 17));
    }

    /** Args: card and uppercase field name. Returns: nothing. */
    private void inputLabel(LinearLayout card, String name) {
        TextView caption = label(name, 11, MUTED, true);
        caption.setLetterSpacing(0.07f);
        card.addView(caption, spaced(0, 5, 6));
    }

    /** Args: card, label and hint. Returns: styled text input. */
    private EditText input(LinearLayout card, String fieldName, String hint) {
        inputLabel(card, fieldName);
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setTextSize(14);
        field.setTextColor(INK);
        field.setHintTextColor(Color.rgb(154, 164, 184));
        field.setHint(hint);
        field.setPadding(dp(13), dp(11), dp(13), dp(11));
        field.setBackground(shape(Color.rgb(247, 249, 253), 14, BORDER, 1));
        card.addView(field, spaced(0, 0, 12));
        return field;
    }

    /** Args: text and whether primary. Returns: styled button. */
    private Button button(String title, boolean primary) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(primary ? Color.WHITE : ACCENT);
        button.setBackground(shape(primary ? ACCENT : Color.rgb(236, 239, 255), 14, 0, 0));
        button.setMinHeight(dp(48));
        button.setElevation(primary ? dp(2) : 0);
        return button;
    }

    /** Args: string, size, colour and bold. Returns: styled text view. */
    private TextView label(String value, int size, int color, boolean bold) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextSize(size);
        label.setTextColor(color);
        label.setTypeface(bold ? Typeface.create("sans-serif-medium", Typeface.NORMAL)
            : Typeface.create("sans-serif", Typeface.NORMAL));
        return label;
    }

    /** Args: none. Returns: vertical container. */
    private LinearLayout column() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        return c;
    }

    /** Args: top and bottom margin in dp, and ignored reserved spacing. Returns: layout parameters. */
    private LinearLayout.LayoutParams spaced(int unused, int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(top);
        params.bottomMargin = dp(bottom);
        return params;
    }

    /** Args: fill colour, corner radius in dp, border colour, border width in dp. Returns: drawable. */
    private GradientDrawable shape(int colour, int radius, int border, int borderSize) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(colour);
        drawable.setCornerRadius(dp(radius));
        if (borderSize > 0) drawable.setStroke(dp(borderSize), border);
        return drawable;
    }

    /** Args: dp. Returns: device-specific pixels. */
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    /** Args: none. Returns: whether the notification listener is enabled. */
    private boolean isListenerEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        ComponentName name = new ComponentName(this, IncomingNotificationService.class);
        if (enabled == null) return false;
        for (String entry : enabled.split(":")) {
            if (name.equals(ComponentName.unflattenFromString(entry))) return true;
        }
        return false;
    }

    /** Args: toast text. Returns: nothing. */
    private void toast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }
}
