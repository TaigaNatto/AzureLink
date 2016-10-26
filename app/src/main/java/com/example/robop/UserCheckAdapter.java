package com.example.robop;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * Created by taiga on 2016/10/26.
 */

public class UserCheckAdapter extends ArrayAdapter<keyManager> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public UserCheckAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final keyManager currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);

        return row;
    }

    public void getAzure(int position,Context context) {

        final keyManager listItem = getItem(position);

        boolean master = listItem.isMaster();

        if(master) {

            Toast.makeText(context, "true", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context, "false", Toast.LENGTH_SHORT).show();
        }
    }

}
