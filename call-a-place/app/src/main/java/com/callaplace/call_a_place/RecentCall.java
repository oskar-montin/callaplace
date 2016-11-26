package com.callaplace.call_a_place;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class RecentCall implements Comparable<RecentCall> {

    public CallType type;
    public LatLng loc;
    public Date time;

    @Override
    public int compareTo(RecentCall another) {
        return time.compareTo(another.time);
    }
}
