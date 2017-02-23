package com.launcher.mummu.cabclient.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by muhammed on 2/23/2017.
 */

public class DateDiff {
    public static final int DATE_BEFORE = 1;
    public static final int DATE_EQUAL = 0;
    public static final int DATE_AFTER = 2;

    public static int compareDates(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = sdf.parse(startDate);
            Date date2 = sdf.parse(endDate);

            if (date1.after(date2)) {
                System.out.println("Date1 is after Date2");
                return DATE_AFTER;
            }
            if (date1.before(date2)) {
                System.out.println("Date1 is before Date2");
                return DATE_BEFORE;
            }

            if (date1.equals(date2)) {
                System.out.println("Date1 is equal Date2");
                return DATE_EQUAL;
            }
            System.out.println();
        } catch (ParseException ex) {
        }
        return -1;
    }

    public static int compareDates(Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            System.out.println("Date1 is after Date2");
            return DATE_AFTER;
        }
        if (startDate.before(endDate)) {
            System.out.println("Date1 is before Date2");
            return DATE_BEFORE;
        }

        if (startDate.equals(endDate)) {
            System.out.println("Date1 is equal Date2");
            return DATE_EQUAL;
        }
        System.out.println();
        return -1;
    }

    public static boolean isOverDate(long targetDate, int threshold) {
        return new Date().getTime() - targetDate >= threshold * 24 * 60 * 60 * 1000;
    }
}
