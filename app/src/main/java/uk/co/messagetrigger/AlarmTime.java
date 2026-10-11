package uk.co.messagetrigger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Calculates a one-off future alarm in the device's local time zone. */
public final class AlarmTime {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    /** Args: none. Returns: nothing. */
    private AlarmTime() {}

    /** Args: local clock hour and minute. Returns: formatted 24-hour time (HH:mm). */
    public static String format(int hour, int minute) {
        return LocalTime.of(hour, minute).format(TIME);
    }

    /** Args: a local HH:mm time, current instant and zone. Returns: next occurrence in epoch milliseconds. */
    public static long nextOccurrence(String time, Instant now, ZoneId zone) {
        LocalTime localTime = LocalTime.parse(time, TIME);
        LocalDate date = now.atZone(zone).toLocalDate();
        ZonedDateTime candidate = date.atTime(localTime).atZone(zone);
        if (!candidate.toInstant().isAfter(now)) {
            candidate = date.plusDays(1).atTime(localTime).atZone(zone);
        }
        return candidate.toInstant().toEpochMilli();
    }
}
