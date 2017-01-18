package com.cooktogether.helpers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.model.Review;

import java.util.List;

public class ReviewArrayAdapter extends ArrayAdapter<Review> {
    LayoutInflater inflater;

    List<Review> list;

    public ReviewArrayAdapter(Context context, int layoutResourceId, List<Review> data) {
        super(context, layoutResourceId, data);
        list = data;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder = null;
        Review r = getItem(position);

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.item_review, parent, false);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        holder.mark.setText(r.getMark()+"");
        holder.text.setText(r.getText());

        return rowView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Review getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<Review> getData() {
        return list;
    }

    class ViewHolder {
        TextView mark = null;
        TextView text = null;

        ViewHolder(View v) {
            mark = (TextView) v.findViewById(R.id.item_review_mark);
            text = (TextView) v.findViewById(R.id.item_review_text);
        }

    }
}