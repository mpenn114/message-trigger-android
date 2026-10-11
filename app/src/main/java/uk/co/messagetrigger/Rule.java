package uk.co.messagetrigger;

/** Each non-blank predicate must match; rules are OR-ed together. */
public final class Rule {
    public final String id;
    public final String platform;
    public final String senderContains;
    public final String conversationContains;
    public final String bodyContains;
    public final boolean enabled;

    /** Args: ID, platform, sender filter, chat filter, body filter and enabled. Returns: immutable rule. */
    public Rule(String id, String platform, String senderContains, String conversationContains,
                String bodyContains, boolean enabled) {
        this.id = id;
        this.platform = platform;
        this.senderContains = senderContains;
        this.conversationContains = conversationContains;
        this.bodyContains = bodyContains;
        this.enabled = enabled;
    }
}
