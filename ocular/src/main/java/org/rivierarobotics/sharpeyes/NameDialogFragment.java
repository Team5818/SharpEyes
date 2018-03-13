package org.rivierarobotics.sharpeyes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class NameDialogFragment extends DialogFragment {


    public interface NDFCallback {

        void onText(String text);

    }

    public static final String ARG_TITLE = "title";
    public static final String ARG_MESSAGE = "message";
    public static final String ARG_TYPE = "editTextType";

    public static Bundle create(String title, String message, int type) {
        Bundle b = new Bundle(NameDialogFragment.class.getClassLoader());
        b.putString(ARG_TITLE, title);
        b.putString(ARG_MESSAGE, message);
        b.putInt(ARG_TYPE, type);
        return b;
    }

    private NDFCallback callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(getArguments().getString(ARG_TITLE));
        dialog.setMessage(getArguments().getString(ARG_MESSAGE));
        dialog.setNegativeButton(android.R.string.cancel, null);

        View view = getActivity().getLayoutInflater().inflate(R.layout.edit_text_item, null);

        EditText text = view.findViewById(R.id.textItem);
        text.setInputType(getArguments().getInt(ARG_TYPE));

        dialog.setPositiveButton(R.string.create, (d, p) -> callback.onText(text.getText().toString()));
        dialog.setView(view);
        return dialog.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NDFCallback) {
            callback = (NDFCallback) context;
        } else {
            throw new IllegalStateException("NDFCallback required!");
        }
    }
}
