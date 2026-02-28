package io.iztec.tp.commons.core.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Shared date/time helpers.
 * All integrations store timestamps in UTC.
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private DateTimeUtils() {}

    public static String format(Instant instant) {
        return ISO_FORMATTER.format(instant);
    }

    public static Instant now() {
        return Instant.now();
    }
}

