package org.rivierarobotics.sharpeyes.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.rivierarobotics.sharpeyes.DataSelector;
import org.rivierarobotics.sharpeyes.R;
import org.rivierarobotics.sharpeyes.RegionalSelectorActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.ViewHolder> {

    public static final int REQUEST_CODE = 0xF001;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView image;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            image = v.findViewById(R.id.image);
        }
    }

    private final List<T> dataset = new ArrayList<>();
    private final Activity activity;
    private final Function<DataSelector, Collection<T>> selectData;
    private final Function<T, String> getName;
    private final Function<T, Bitmap> getIcon;
    private final BiFunction<T, DataSelector, DataSelector> onSelection;
    private final Class<?> activityClass;
    private DataSelector selector;

    public GenericAdapter(Activity activity,
                          Function<DataSelector, Collection<T>> selectData,
                          Function<T, String> getName,
                          Function<T, Bitmap> getIcon,
                          BiFunction<T, DataSelector, DataSelector> onSelection,
                          Class<?> activityClass) {
        this.activity = activity;
        this.selectData = selectData;
        this.getName = getName;
        this.getIcon = getIcon;
        this.onSelection = onSelection;
        this.activityClass = activityClass;
    }

    public void initialize(DataSelector selector) {
        dataset.clear();
        this.selector = selector;
        dataset.addAll(selectData.apply(selector));
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon_and_text_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        T item = dataset.get(position);
        holder.title.setText(getName.apply(item));
        Bitmap icon = getIcon.apply(item);
        if (icon != null) {
            holder.image.setImageBitmap(icon);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, activityClass);
            onSelection.apply(item, selector).saveTo(intent);
            activity.startActivityForResult(intent, REQUEST_CODE);
        });
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
