package uk.co.messagetrigger;

/** A notification-derived incoming message; never persisted to disk. */
public final class MessageData {
    public final String platform;
    public final String sender;
    public final String conversation;
    public final String body;
    public final long timestamp;

    /** Args: platform, sender, conversation, body and timestamp. Returns: a message value. */
    public MessageData(String platform, String sender, String conversation, String body, long timestamp) {
        this.platform = safe(platform);
        this.sender = safe(sender);
        this.conversation = safe(conversation);
        this.body = safe(body);
        this.timestamp = timestamp;
    }

    /** Args: nullable text. Returns: non-null text. */
    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
