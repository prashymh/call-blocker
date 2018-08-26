package com.dsoft.shetty.callblocker;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Reject Log Adapter
 */

class RejectLogAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<RejectLog> rejectLogs;

        public RejectLogAdapter(Context context, ArrayList<RejectLog> rejectLogs){
            this.context = context;
            this.rejectLogs = rejectLogs;
        }

        @Override
        public int getCount(){
            return rejectLogs.size();
        }

        @Override
        public Object getItem(int position){
            return rejectLogs.get(position);
        }

        @Override
        public long getItemId(int position){
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View ret_view;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ret_view = (View) inflater.inflate(android.R.layout.simple_list_item_2, null);
            } else {
                ret_view = convertView;
            }

            int length = rejectLogs.size() - 1;
            TextView text1 = ret_view.findViewById(android.R.id.text1);
            TextView text2 = ret_view.findViewById(android.R.id.text2);

            text1.setText(rejectLogs.get(length - position).getPhoneNumber());
            text2.setText(rejectLogs.get(length - position).getDateTimeOfCall());
            text1.setTypeface(null, Typeface.BOLD);
            text2.setTextColor(ContextCompat.getColor(context,R.color.colorTime));
            return ret_view;
        }

    }

