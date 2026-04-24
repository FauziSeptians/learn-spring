package com.absensi.absensi_app.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Simpan semua formatter di 1 tempat
public class DateTimeFormatterUtil {

    private static final Locale LOCALE_ID = new Locale("id", "ID");

    // "24 Maret 2026"
    public static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", LOCALE_ID);

    // "24 Maret 2026 10:30:45"
    public static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss", LOCALE_ID);

    // "24 Maret 2026 10:30"
    public static final DateTimeFormatter DATE_TIME_NO_SECOND_FORMAT =
        DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", LOCALE_ID);

    // "Selasa, 24 Maret 2026 10:30"
    public static final DateTimeFormatter DATE_TIME_FULL_FORMAT =
        DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm", LOCALE_ID);

    private DateTimeFormatterUtil() {}
}
