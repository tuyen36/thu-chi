package com.example.thuchi;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

/** Tien te VN + moc thang. */
public final class Money {
    private static final NumberFormat FMT =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    private Money() {}

    public static String vnd(long v) {
        return FMT.format(v) + "₫";
    }

    /** Parse "1.200.000" / "1200000" -> 1200000. */
    public static long parse(String s) {
        if (s == null) return 0;
        String d = s.replaceAll("[^0-9]", "");
        if (d.isEmpty()) return 0;
        try {
            return Long.parseLong(d);
        } catch (Exception e) {
            return 0;
        }
    }

    /** [from, to) cua thang chua millis. */
    public static long[] monthRange(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long from = c.getTimeInMillis();
        c.add(Calendar.MONTH, 1);
        return new long[]{from, c.getTimeInMillis()};
    }

    /** Dau ngay (00:00) cua millis. */
    public static long dayStart(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
