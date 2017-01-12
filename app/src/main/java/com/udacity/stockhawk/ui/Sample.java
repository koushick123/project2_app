package com.udacity.stockhawk.ui;

import java.util.Calendar;

/**
 * Created by Koushick on 11-01-2017.
 */
public class Sample {

    public static void main(String[] args){

        Calendar cal = Calendar.getInstance();
        System.out.println(cal.get(Calendar.DATE)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.YEAR));
        cal.add(Calendar.WEEK_OF_YEAR,-2);
        System.out.println(cal.get(Calendar.DATE)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.YEAR));
    }
}
