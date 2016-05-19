package com.callaplace.call_a_place;

import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Marcus on 2016-05-17.
 */
public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.ViewHolder> {

    private final JSONArray mCallHistory;

    class ViewHolder extends RecyclerView.ViewHolder {
        private JSONObject mCall;
        private final View root;
        private final TextView title, sub;
        private final ImageView icon;
        private final ImageButton redial;
        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            title = (TextView) itemView.findViewById(R.id.callCardTitle);
            sub = (TextView) itemView.findViewById(R.id.callCardSubTitle);
            icon = (ImageView) itemView.findViewById(R.id.callCardIcon);
            redial = (ImageButton) itemView.findViewById(R.id.redialButton);
        }

        public void setCall(JSONObject call) {
            this.mCall = call;
            root.setTag(call);
            redial.setTag(call);
        }
    }

    public CallHistoryAdapter(JSONArray callHistory) {
        mCallHistory = callHistory;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_history_item, parent, false));
        //onCallSelectedListener
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject call = mCallHistory.getJSONObject(position);
            holder.setCall(call);
            holder.title.setText(call.getString("type") + " call");
            String time = call.getString("time");
            holder.sub.setText(call.getJSONArray("loc").getDouble(0) + ", " +
                    call.getJSONArray("loc").getDouble(1) + ", " + time);
            int iconResource = 0;
            switch (MainActivity.CallType.valueOf(call.getString("type"))){
                case INCOMING:
                    iconResource = R.drawable.ic_call_received_green_24dp;
                    break;
                case OUTGOING:
                    iconResource = R.drawable.ic_call_made_green_24dp;
                    break;
                case MISSED:
                    iconResource = R.drawable.ic_call_missed_red_24dp;
                    break;
            }
            holder.icon.setImageDrawable(holder.icon.getContext().getDrawable(iconResource));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mCallHistory.length();
    }
}
