package com.callaplace.call_a_place;

import com.google.android.gms.maps.model.LatLng;

public class Favorite {

    public LatLng loc;

    @Override
    public int hashCode() {
        return loc.hashCode();
    }
}
