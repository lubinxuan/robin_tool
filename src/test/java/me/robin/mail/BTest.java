package me.robin.mail;

import java.util.Calendar;

/**
 * Created by Administrator on 2015/10/27.
 */
public class BTest {
    public static void main(String[] args) {

        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.get(Calendar.YEAR));
        System.out.println(calendar.get(Calendar.MONTH));
        System.out.println(calendar.get(Calendar.DAY_OF_MONTH));

    }
}
