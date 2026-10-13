package com.bolao2027.shared.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    public static final ZoneId BRT_ZONE = ZoneId.of("America/Sao_Paulo");
    public static final DateTimeFormatter BRT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateUtils() {
    }

    public static LocalDateTime nowBrt() {
        return ZonedDateTime.now(BRT_ZONE).toLocalDateTime();
    }

    public static LocalDateTime calculateCutoffTime(LocalDateTime matchDateTime) {
        return matchDateTime.minusHours(1);
    }

    public static boolean isMatchLocked(LocalDateTime matchDateTime) {
        return nowBrt().isAfter(calculateCutoffTime(matchDateTime));
    }

    public static String formatBrt(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(BRT_FORMATTER) : "-";
    }

    public static String formatTimeOnly(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_ONLY_FORMATTER) : "-";
    }

    public static String formatDateOnly(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_ONLY_FORMATTER) : "-";
    }

    public static long minutesUntilCutoff(LocalDateTime matchDateTime) {
        LocalDateTime cutoff = calculateCutoffTime(matchDateTime);
        LocalDateTime now = nowBrt();
        if (now.isAfter(cutoff)) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(now, cutoff);
    }

    /**
     * Returns the Unix epoch second (UTC) of the cutoff time for the given match.
     * matchDateTime is stored as BRT (America/Sao_Paulo, UTC-3).
     */
    public static long cutoffEpoch(LocalDateTime matchDateTime) {
        LocalDateTime cutoff = calculateCutoffTime(matchDateTime);
        return cutoff.atZone(BRT_ZONE).toEpochSecond();
    }
}
