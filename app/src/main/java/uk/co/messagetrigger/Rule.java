package uk.co.messagetrigger;

/** A message filter and its alarm action. Empty alarmTime means ring immediately. */
public final class Rule {
    public final String id;
    public final String platform;
    public final String senderContains;
    public final String conversationContains;
    public final String bodyContains;
    public final boolean enabled;
    public final String alarmTime;

    /** Args: ID, platform, sender filter, chat filter, body filter, enabled and local HH:mm time. Returns: immutable rule. */
    public Rule(String id, String platform, String senderContains, String conversationContains,
                String bodyContains, boolean enabled, String alarmTime) {
        this.id = id;
        this.platform = platform;
        this.senderContains = senderContains;
        this.conversationContains = conversationContains;
        this.bodyContains = bodyContains;
        this.enabled = enabled;
        this.alarmTime = alarmTime == null ? "" : alarmTime;
    }

    /** Args: original rule fields. Returns: a backward-compatible immediate-alarm rule. */
    public Rule(String id, String platform, String senderContains, String conversationContains,
                String bodyContains, boolean enabled) {
        this(id, platform, senderContains, conversationContains, bodyContains, enabled, "");
    }
}
