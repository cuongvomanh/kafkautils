package com.mycompany.mygroup.utils;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public abstract class StringUtils {

    public static String reverseString(String input) {
        StringBuilder backwards = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            backwards.append(input.charAt(input.length() - 1 - i));
        }
        return backwards.toString();
    }

    public static Date truncDate(Date dt) {
        Calendar cal= Calendar.getInstance();
        cal.setTime(dt);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long time=cal.getTimeInMillis();
        Date newDt=new Date(time);
        return newDt;
    }

    public static String truncMilisecondDate(String minisecondDateStr) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/MM/dd KK:mm:ss", Locale.UK);
        Date dt = new Date(Long.parseLong(minisecondDateStr));
        Date newDt = truncDate(dt);
        return simpleDateFormat.format(newDt);
    }

    public static String truncDate(String dateStr) throws ParseException {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/MM/dd KK:mm:ss", Locale.UK);
        Date dt = simpleDateFormat.parse(dateStr);
        simpleDateFormat.format(dt);
        Date newDt = truncDate(dt);
        return simpleDateFormat.format(newDt);
    }
}
