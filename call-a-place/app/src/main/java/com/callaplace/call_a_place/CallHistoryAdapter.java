package com.callaplace.call_a_place;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.ViewHolder> {

    private final Geocoder mGeocoder;
    private final List<RecentCall> mCallHistory;
    private OnCallSelectedListener mOnCallSelectedListener;

    public void setOnCallSelectedListener(OnCallSelectedListener onCallSelectedListener) {
        mOnCallSelectedListener = onCallSelectedListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RecentCall mCall;
        private final TextView mTitleView, mSubTitleView;
        private final ImageView mIconView;
        public ViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.callCardTitle);
            mSubTitleView = (TextView) itemView.findViewById(R.id.callCardSubTitle);
            mIconView = (ImageView) itemView.findViewById(R.id.callCardIcon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnCallSelectedListener != null) {
                        mOnCallSelectedListener.onSelected(mCall);
                    }
                }
            });
        }

        public void setCall(RecentCall call) {
            mCall = call;
            mTitleView.setText(getMarkerAddress(call.loc));
            mSubTitleView.setText(String.format("%.3f, %.3f, %s",
                    call.loc.latitude, call.loc.longitude,
                    new SimpleDateFormat("dd MMM").format(call.time)));
            mIconView.setImageDrawable(itemView.getContext().getDrawable(call.type.getIcon()));
        }
    }
    interface OnCallSelectedListener {
        void onSelected(RecentCall call);
    }

    public CallHistoryAdapter(List<RecentCall> callHistory, Geocoder geocoder) {
        mCallHistory = callHistory;
        mGeocoder = geocoder;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.call_history_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setCall(mCallHistory.get(position));
    }

    private String getMarkerAddress(LatLng latLng) {
        try {
            Address address = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            return address.getThoroughfare() + " " + address.getFeatureName();
        } catch (IOException | IndexOutOfBoundsException e) {
            return "Custom location";
        }
    }

    @Override
    public int getItemCount() {
        return mCallHistory.size();
    }
}
