package uk.co.messagetrigger;
import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
/** Rule engine tests. */
public final class RuleMatcherTest {
    /** Args: none. Returns: nothing; validates conjunction and case insensitivity. */
    @Test public void matchesAllConfiguredPredicatesIgnoringCase(){
        Rule r=new Rule("1","WhatsApp","alice","family","urgent",true);
        assertTrue(RuleMatcher.matches(r,new MessageData("WhatsApp","Alice P","Family Group","URGENT call me",0)));
        assertFalse(RuleMatcher.matches(r,new MessageData("WhatsApp","Bob","Family Group","urgent",0)));
        assertFalse(RuleMatcher.matches(r,new MessageData("Teams","Alice","Family Group","urgent",0)));
    }
    /** Args: none. Returns: nothing; checks blank wildcards. */
    @Test public void blankPredicatesAreWildcards(){
        Rule r=new Rule("2","SMS","","","code",true);
        assertTrue(RuleMatcher.matches(r,new MessageData("SMS","+447700900000","","Your CODE is 1234",0)));
    }
    /** Args: none. Returns: nothing; checks disabled rules. */
    @Test public void disabledRuleDoesNotActivate(){
        Rule r=new Rule("3","Teams","","","meeting",false);
        assertFalse(RuleMatcher.matches(r,new MessageData("Teams","Boss","","meeting",0)));
    }
    /** Args: none. Returns: nothing; checks OR selection. */
    @Test public void choosesFirstMatchingRule(){
        Rule a=new Rule("a","WhatsApp","","","alpha",true);
        Rule b=new Rule("b","WhatsApp","","","beta",true);
        assertSame(b,RuleMatcher.firstMatch(Arrays.asList(a,b),new MessageData("WhatsApp","A","B","BETA",0)));
    }
}
