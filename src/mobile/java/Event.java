package com.williams_research.pebble_shake;

/**
 * Created by gmfinnie33 on 12/2/15.
 *
 * Holds the data of some gesture.
 *
 * I. Time
 * II. Acceleration Data
 *
 * [use Query(Event e) to determine nature of Event e.]
 *
 */
public class Event {
    private final int TIME_KEY = 0;

    private int time;
    private int[] accel_series;
    private int[] data;

    public Event( int[] dat ){
        // store the data of the event
        data = dat;
        time = dat[TIME_KEY];
        // accel_series
    }

    protected int getTime(){
        return time;
    }

    protected int[] getSeries(){
        return accel_series;
    }



}
