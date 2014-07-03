package com.twistedplane.sealnote.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A helper class to easily get dates and times for use in notes and views.
 * Appropriate conversions from current timezone to GMT is made behind the
 * scenes.
 */
public class EasyDate {
    private final static String mDF_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";

    private Date mDate;

    /**
     * Return EasyDate object containing current timestamp.
     */
    public static EasyDate now() {
        return new EasyDate(new Date());
    }

    /**
     * Convert a date given in ISO format to EasyDate object
     *
     * @param date  String containing date in ISO format
     * @return      EasyDate object with given date
     * @throws      ParseException
     */
    public static EasyDate fromIsoString(String date) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(mDF_ISO_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new EasyDate(df.parse(date));
    }

    /**
     * Create a EasyDate object using given java Date object
     */
    private EasyDate(Date date) {
        mDate = date;
    }

    /**
     * Convert current EasyDate object to ISO format string with UTC
     * as timezone.
     *
     * @return  String containing ISO format UTC time.
     */
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat(mDF_ISO_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(mDate);
    }

    /**
     * Return a string containing datetime in user friendly easy to read
     * form.
     *
     * + If the date equal to today, just show time. Eg "12:40 PM"
     * + If year is equal to current year. Eg. "24th July"
     * + Else show date. Eg. "24th July 2011"
     */
    public String friendly() {
        Calendar now = Calendar.getInstance();
        Calendar previous = Calendar.getInstance();
        SimpleDateFormat df;

        previous.setTime(mDate);

        if (previous.get(Calendar.DATE) == now.get(Calendar.DATE) &&
                previous.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                previous.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            df = new SimpleDateFormat("hh:mm aa");
        } else if (previous.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            df = new SimpleDateFormat("MMM dd");

        } else {
            df = new SimpleDateFormat("MMM dd, yyyy");
        }
        df.setTimeZone(TimeZone.getDefault());
        return df.format(mDate);
    }
}
