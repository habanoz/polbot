package com.habanoz.polbot.core.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Yuce on 5/5/2017.
 */
public class DateUtil {

    public static  LocalDateTime toLdt(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        ZonedDateTime zdt = cal.toZonedDateTime();
        return zdt.toLocalDateTime();
    }

    public static  Date fromLdt(LocalDateTime ldt) {
        ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
        GregorianCalendar cal = GregorianCalendar.from(zdt);
        return cal.getTime();
    }

}
