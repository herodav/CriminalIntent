package org.examples.hervekabamba.criminalintent.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static Date parseDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        return sdf.parse(date);
    }

    public static String formatDate(String date){
       Date parsedDate;
        try {
            parsedDate= parseDate(date);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd");
            return sdf.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatTime(String time) {
        Date parsedDate;
        try {
            parsedDate= parseDate(time);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:MM");
            return sdf.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
