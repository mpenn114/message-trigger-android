package uk.co.messagetrigger;

import java.util.List;
import java.util.Locale;

/** Pure-Java matcher, independent of the Android notification framework. */
public final class RuleMatcher {
    /** Args: none. Returns: nothing; utility classes cannot be instantiated. */
    private RuleMatcher() { }

    /** Args: a rule and a message. Returns: true if all configured filters match. */
    public static boolean matches(Rule rule, MessageData message) {
        return rule.enabled
                && rule.platform.equals(message.platform)
                && contains(message.sender, rule.senderContains)
                && contains(message.conversation, rule.conversationContains)
                && contains(message.body, rule.bodyContains);
    }

    /** Args: rules and an incoming message. Returns: the first matching rule or null. */
    public static Rule firstMatch(List<Rule> rules, MessageData message) {
        for (Rule rule : rules) {
            if (matches(rule, message)) return rule;
        }
        return null;
    }

    /** Args: source and required substring. Returns: whether the substring occurs, ignoring case. */
    private static boolean contains(String source, String required) {
        return required == null || required.trim().isEmpty()
                || source.toLowerCase(Locale.ROOT).contains(required.trim().toLowerCase(Locale.ROOT));
    }
}
