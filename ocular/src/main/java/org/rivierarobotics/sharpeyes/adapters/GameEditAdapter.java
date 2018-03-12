package org.rivierarobotics.sharpeyes.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class GameEditAdapter extends RecyclerView.Adapter<GameEditAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public ViewHolder(TextView v) {
            super(v);
            title = v;
        }
    }

    public static final class Item {
        private final String name;
        private final View.OnClickListener callback;

        public Item(String name, View.OnClickListener callback) {
            this.name = name;
            this.callback = callback;
        }
    }

    private final List<Item> dataset = new ArrayList<>();

    public void addItem(Item item) {
        int index = dataset.size();
        dataset.add(item);
        notifyItemInserted(index);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item setting = dataset.get(position);
        holder.title.setText(setting.name);

        holder.itemView.setOnClickListener(setting.callback);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
