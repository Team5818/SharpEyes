package org.rivierarobotics.sharpeyes.adapters;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Iterables;

import org.rivierarobotics.sharpeyes.R;

import java.util.ArrayList;
import java.util.List;


public class ReceiveDeviceAdapter extends RecyclerView.Adapter<ReceiveDeviceAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textItem;

        public ViewHolder(View v) {
            super(v);
            textItem = v.findViewById(R.id.textItem);
        }
    }

    public static final class Item {

        public enum Status {
            AVAILABLE, QUEUED, FETCHING, FAILED, COMPLETE
        }

        private final String name;
        private final View.OnClickListener callback;
        private Status status = Status.AVAILABLE;

        public Item(String name, View.OnClickListener callback) {
            this.name = name;
            this.callback = callback;
        }
    }

    private final List<Item> dataset = new ArrayList<>();
    private final Activity activity;

    public ReceiveDeviceAdapter(Activity activity) {
        this.activity = activity;
    }

    public int addItem(Item item) {
        int index = dataset.size();
        if (Iterables.any(dataset, i -> i.name.equals(item.name))) {
            return -1;
        }
        dataset.add(item);
        notifyItemInserted(index);
        return index;
    }

    public void updateItemStatus(int index, Item.Status status) {
        dataset.get(index).status = status;
        notifyItemChanged(index);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = dataset.get(position);
        holder.textItem.setText(item.name);

        holder.itemView.setOnClickListener(item.callback);

        holder.itemView.setBackgroundColor(statusColor(item.status));
    }

    private int statusColor(Item.Status status) {
        switch (status) {
            case AVAILABLE:
                return ContextCompat.getColor(activity, R.color.status_available);
            case QUEUED:
                return ContextCompat.getColor(activity, R.color.status_queued);
            case FETCHING:
                return ContextCompat.getColor(activity, R.color.status_fetching);
            case FAILED:
                return ContextCompat.getColor(activity, R.color.status_failed);
            case COMPLETE:
                return ContextCompat.getColor(activity, R.color.status_complete);
        }
        return ContextCompat.getColor(activity, R.color.bluetooth);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
