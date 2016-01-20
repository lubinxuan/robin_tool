package me.robin.api.entity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lubin.Xuan on 2015/10/12.
 * ie.
 */
public class DateEntity extends Entity<Date> {
    private final DateFormat dateFormat;

    public DateEntity(String key, String dateFormat) {
        this(key, key, dateFormat);
    }

    public DateEntity(String key, String mapping, String dateFormat) {
        super(key, mapping);
        this.dateFormat = new SimpleDateFormat(dateFormat);
    }

    @Override
    public Date value(Object value) {
        if (null == value) {
            return null;
        }

        String str = String.valueOf(value);
        if (str.length() < 1) {
            return null;
        }

        try {
            synchronized (dateFormat) {
                return dateFormat.parse(String.valueOf(value));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
