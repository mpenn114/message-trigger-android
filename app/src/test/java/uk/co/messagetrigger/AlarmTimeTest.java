package uk.co.messagetrigger;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.time.Instant;
import java.time.ZoneId;

/** Unit tests for local-time, next-occurrence scheduling. */
public final class AlarmTimeTest {
    private static final ZoneId LONDON = ZoneId.of("Europe/London");

    /** Args: none. Returns: nothing; checks an upcoming alarm occurs today. */
    @Test public void upcomingTimeSchedulesToday() {
        long at = AlarmTime.nextOccurrence("06:00", Instant.parse("2026-10-11T04:00:00Z"), LONDON);
        assertEquals(Instant.parse("2026-10-11T05:00:00Z").toEpochMilli(), at);
    }

    /** Args: none. Returns: nothing; checks a past time occurs tomorrow. */
    @Test public void pastTimeSchedulesTomorrow() {
        long at = AlarmTime.nextOccurrence("06:00", Instant.parse("2026-10-11T06:00:00Z"), LONDON);
        assertEquals(Instant.parse("2026-10-12T05:00:00Z").toEpochMilli(), at);
    }

    /** Args: none. Returns: nothing; checks equality schedules the next day. */
    @Test public void exactlyAtTimeSchedulesTomorrow() {
        long at = AlarmTime.nextOccurrence("06:00", Instant.parse("2026-10-11T05:00:00Z"), LONDON);
        assertEquals(Instant.parse("2026-10-12T05:00:00Z").toEpochMilli(), at);
    }

    /** Args: none. Returns: nothing; checks daylight-saving offset changes correctly. */
    @Test public void calculatesAcrossDaylightSavingBoundary() {
        long at = AlarmTime.nextOccurrence("06:00", Instant.parse("2026-10-24T07:00:00Z"), LONDON);
        assertEquals(Instant.parse("2026-10-25T06:00:00Z").toEpochMilli(), at);
    }

    /** Args: none. Returns: nothing; checks formatting from a time picker. */
    @Test public void displaysPaddedClockTime() {
        assertEquals("06:05", AlarmTime.format(6, 5));
    }

    /** Args: none. Returns: nothing; checks midnight is a supported time. */
    @Test public void supportsMidnight() {
        long at = AlarmTime.nextOccurrence("00:00", Instant.parse("2026-10-11T20:00:00Z"), LONDON);
        assertEquals(Instant.parse("2026-10-11T23:00:00Z").toEpochMilli(), at);
    }
}
