package com.twistedplane.sealnote.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class EasyDate {
    private final static String mDF_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";

    private Date mDate;

    public static EasyDate now() {
        return new EasyDate(new Date());
    }

    public static EasyDate fromIsoString(String date) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(mDF_ISO_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new EasyDate(df.parse(date));
    }

    private EasyDate() {
        mDate = new Date(0);
    }

    private EasyDate(Date date) {
        mDate = date;
    }

    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat(mDF_ISO_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(mDate);
    }

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
