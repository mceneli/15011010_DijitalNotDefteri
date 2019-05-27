package com.muhammet.notepad.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import com.muhammet.notepad.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DeleteDialogFragment extends DialogFragment {
    public interface Listener {
        void onDeleteDialogPositiveClick();
    }

    Listener listener;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_are_you_sure)
        .setTitle(getArguments().getInt("dialog_title"))
        .setPositiveButton(R.string.action_delete, (dialog, id) -> listener.onDeleteDialogPositiveClick())
        .setNegativeButton(R.string.action_cancel, null);

        return builder.create();
    }
}
