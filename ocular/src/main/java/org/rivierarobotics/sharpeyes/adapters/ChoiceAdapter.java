package org.rivierarobotics.sharpeyes.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.protobuf.ProtocolStringList;

import java.util.List;

class ChoiceAdapter extends BaseAdapter {
    private final List<String> choices;
    public ChoiceAdapter(List<String> choices) {
        this.choices = choices;
    }

    @Override
    public int getCount() {
        return choices.size();
    }

    @Override
    public String getItem(int position) {
        return choices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextView(parent.getContext());
        }

        ((TextView) convertView).setText(getItem(position));
        return convertView;
    }
}
