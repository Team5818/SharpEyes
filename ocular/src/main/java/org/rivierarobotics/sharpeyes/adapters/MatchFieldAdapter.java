package org.rivierarobotics.sharpeyes.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldValue;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.DataSelector;
import org.rivierarobotics.sharpeyes.R;
import org.rivierarobotics.sharpeyes.common.FieldDefHelper;
import org.rivierarobotics.sharpeyes.gamedb.GameDbAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.rivierarobotics.sharpeyes.Functional.forEach;


public class MatchFieldAdapter extends RecyclerView.Adapter<MatchFieldAdapter.ViewHolder> {

    public abstract static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView label;

        public ViewHolder(View v) {
            super(v);
            label = v.findViewById(R.id.label);
        }

        public abstract void bind(FieldDefinition def, FieldValue value);

        public abstract FieldValue saveValue(FieldDefinition def);
    }

    public static class EditTextViewHolder extends ViewHolder {
        public final EditText textbox;

        public EditTextViewHolder(View v) {
            super(v);
            textbox = v.findViewById(R.id.textbox);
        }

        @Override
        public void bind(FieldDefinition def, FieldValue value) {
            Log.d("MFA", "binding data " + value);
            switch (def.getType()) {
                case INTEGER:
                    textbox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    textbox.setText(String.valueOf(value.getInteger()));
                    break;
                case FLOATING:
                    textbox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    textbox.setText(String.valueOf(value.getFloating()));
                    break;
                case STRING:
                    textbox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
                    textbox.setText(value.getStr());
                    break;
                default:
                    throw new IllegalStateException("Unexpected FieldDefinition value: " + def.getType().name());
            }
        }

        @Override
        public FieldValue saveValue(FieldDefinition def) {
            FieldValue.Builder b = FieldValue.newBuilder();
            switch (def.getType()) {
                case INTEGER:
                    b.setInteger(Long.parseLong(textbox.getText().toString()));
                    break;
                case FLOATING:
                    b.setFloating(Double.parseDouble(textbox.getText().toString()));
                    break;
                case STRING:
                    b.setStr(textbox.getText().toString());
                    break;
                default:
                    throw new IllegalStateException("Unexpected FieldDefinition value: " + def.getType().name());
            }
            return b.build();
        }
    }

    public static class CheckBoxViewHolder extends ViewHolder {
        public final CheckBox checkBox;

        public CheckBoxViewHolder(View v) {
            super(v);
            checkBox = v.findViewById(R.id.checkBox);
        }

        @Override
        public void bind(FieldDefinition def, FieldValue value) {
            checkBox.setChecked(value.getBoole());
        }

        @Override
        public FieldValue saveValue(FieldDefinition def) {
            return FieldValue.newBuilder().setBoole(checkBox.isChecked()).build();
        }
    }

    public static class ChoiceViewHolder extends ViewHolder {
        public final Spinner choices;

        public ChoiceViewHolder(View v) {
            super(v);
            choices = v.findViewById(R.id.choices);
        }

        @Override
        public void bind(FieldDefinition def, FieldValue value) {
            choices.setAdapter(new ChoiceAdapter(def.getChoicesList()));
            int selected = def.getChoicesList().indexOf(value.getStr());
            if (selected < 0) {
                // unknown value, use default
                selected = 0;
            }
            choices.setSelection(selected);
        }

        @Override
        public FieldValue saveValue(FieldDefinition def) {
            return FieldValue.newBuilder().setStr((String) choices.getSelectedItem()).build();
        }
    }

    private final List<FieldDefinition> fieldDefs = new ArrayList<>();
    private GameDbAccess db;
    private DataSelector selector;

    public void initialize(Intent intent) {
        fieldDefs.clear();

        db = GameDbAccess.getInstance();
        selector = DataSelector.loadFrom(intent);

        Map<String, FieldValue> vals = new HashMap<>(db.getTeamMatch(selector).getValuesMap());

        forEach(db.getGame(selector).getBase().getFieldDefsList(), fd -> {
            fieldDefs.add(fd);
            if (!vals.containsKey(fd.getName())) {
                TeamMatch match = db.rebuildTeamMatch(selector, tm -> tm.putValues(fd.getName(), FieldDefHelper.defaultFieldValue(fd)));
                vals.put(fd.getName(), match.getValuesOrThrow(fd.getName()));
            }
        });

        notifyDataSetChanged();
    }

    public GameDbAccess getDb() {
        return db;
    }

    public DataSelector getSelector() {
        return selector;
    }

    public void saveData(RecyclerView view) {
        db.rebuildTeamMatch(selector, tm -> {
            tm.clearValues();
            for (int i = 0; i < fieldDefs.size(); i++) {
                FieldDefinition fd = fieldDefs.get(i);
                ViewHolder holder = (ViewHolder) view.findViewHolderForAdapterPosition(i);
                if (holder == null) {
                    continue;
                }
                Log.d("MatchSave", "Saving value " + fd.getName() + ": " + holder.saveValue(fd));
                tm.putValues(fd.getName(), holder.saveValue(fd));
            }
            return tm;
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        switch (viewType) {
            case R.layout.field_value_item_choice:
                return new ChoiceViewHolder(view);
            case R.layout.field_value_item_bool:
                return new CheckBoxViewHolder(view);
            default:
                return new EditTextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FieldDefinition def = fieldDefs.get(position);
        holder.label.setText(def.getName());
        FieldValue value = db.getTeamMatch(selector).getValuesOrThrow(def.getName());

        holder.bind(def, value);
    }

    @Override
    public int getItemViewType(int position) {
        switch (fieldDefs.get(position).getType()) {
            case CHOICE:
                return R.layout.field_value_item_choice;
            case BOOLEAN:
                return R.layout.field_value_item_bool;
            default:
                return R.layout.field_value_item;
        }
    }

    @Override
    public int getItemCount() {
        return fieldDefs.size();
    }
}
