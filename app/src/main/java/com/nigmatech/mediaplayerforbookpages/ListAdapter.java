package com.nigmatech.mediaplayerforbookpages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/** Created by Hany Mohamed Nigma on 02.2020.
 * Website : https://www.youtube.com/nigmatech
 * our channel on YouTube : https://www.youtube.com/nigmatech
 * our page on Facebook : https://www.facebook.com/NigmaTechYT/
 * our group on Facebook : https://www.facebook.com/groups/hanynigma/
 */

public class ListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private Context context;
    private List<ListViewData> listData = null;

    ListAdapter(Activity context, List<ListViewData> listData) {
        this.context = context;
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
        ArrayList<ListViewData> arrayList = new ArrayList<>();
        arrayList.addAll(listData);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        @SuppressLint("ViewHolder") View Item = layoutInflater.inflate(R.layout.list_view_design, null, true);

        TextView mediaNameTextView = Item.findViewById(R.id.mediaNameTextView);
        TextView mediaNumberTextView = Item.findViewById(R.id.mediaNumberTextView);

        int i = position+1;

        mediaNameTextView.setText(listData.get(position).getMediaName());

        mediaNumberTextView.setText("﴾"+i+"﴿");

        return Item;
    }


}
