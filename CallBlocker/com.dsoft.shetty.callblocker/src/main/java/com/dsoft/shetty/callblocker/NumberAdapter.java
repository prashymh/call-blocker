package com.dsoft.shetty.callblocker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom Adapter for the blocked number list in List View
 */

class NumberAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<BlockNumber> blockNumbers;

    public NumberAdapter(Context context, ArrayList<BlockNumber> blockNumbers){
        this.context = context;
        this.blockNumbers = blockNumbers;
    }

    @Override
    public int getCount(){
        return blockNumbers.size();
    }

    @Override
    public Object getItem(int position){
        return blockNumbers.get(position);
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
            ret_view = (View) inflater.inflate(R.layout.list_item, null);
        } else {
            ret_view = convertView;
        }

        TextView text1 = ret_view.findViewById(R.id.text1);
        TextView text2 = ret_view.findViewById(R.id.text2);

        text1.setText(blockNumbers.get(position).getPattern());
        text2.setText(blockNumbers.get(position).getPhoneNumber());

        ImageButton button = ret_view.findViewById(R.id.delete);
        button.setTag(position);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Integer index = (Integer) v.getTag();
                blockNumbers.remove(position);
                notifyDataSetChanged();
                if(context instanceof MainActivity) {
                    ((MainActivity) context).saveBlockList();
                }
            }
        });

        return ret_view;
    }

}
