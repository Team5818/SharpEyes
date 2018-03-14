package org.rivierarobotics.sharpeyes.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;

import org.rivierarobotics.sharpeyes.BiFunction;
import org.rivierarobotics.sharpeyes.DataSelector;
import org.rivierarobotics.sharpeyes.R;
import org.rivierarobotics.sharpeyes.gamedb.GameDbAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SelectorAdapter<T> extends RecyclerView.Adapter<SelectorAdapter.ViewHolder> {

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

    public interface AddSelectionCallback {

        void add(DataSelector selector);

    }

    private final List<T> dataset = new ArrayList<>();
    private final Activity activity;
    private final Comparator<T> comparator;
    private final Function<GameDbAccess, Function<DataSelector, Collection<T>>> selectData;
    private final Function<T, String> getName;
    private final Function<T, Bitmap> getIcon;
    private final BiFunction<T, DataSelector, DataSelector> onSelection;
    private final AddSelectionCallback selectionCb;
    private final Class<?> activityClass;
    private GameDbAccess db;
    private DataSelector selector;

    public SelectorAdapter(Activity activity,
                           Comparator<T> comparator,
                           Function<GameDbAccess, Function<DataSelector, Collection<T>>> selectData,
                           Function<T, String> getName,
                           Function<T, Bitmap> getIcon,
                           BiFunction<T, DataSelector, DataSelector> onSelection,
                           AddSelectionCallback selectionCb,
                           Class<?> activityClass) {
        this.activity = activity;
        this.comparator = comparator;
        this.selectData = selectData;
        this.getName = getName;
        this.getIcon = getIcon;
        this.onSelection = onSelection;
        this.selectionCb = selectionCb;
        this.activityClass = activityClass;
    }

    public GameDbAccess getDb() {
        return db;
    }

    public DataSelector getSelector() {
        return selector;
    }

    public void initialize(Intent intent) {
        initialize(GameDbAccess.getInstance(), DataSelector.loadFrom(intent));
    }

    public void initialize(GameDbAccess db, DataSelector selector) {
        this.db = db;
        this.selector = selector;
        reloadData();
        hookAdd();
    }

    private void hookAdd() {
        Button add = activity.findViewById(R.id.pickAdd);
        if (add == null || selectionCb == null) {
            return;
        }
        add.setOnClickListener(view -> {
            selectionCb.add(selector);
            reloadData();
        });
    }

    public void reloadData() {
        dataset.clear();
        dataset.addAll(selectData.apply(db).apply(selector));
        Collections.sort(dataset, comparator);
        notifyDataSetChanged();
    }

    public void onReloadRequest() {
        reloadData();
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
