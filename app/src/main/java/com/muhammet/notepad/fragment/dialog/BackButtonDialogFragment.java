package com.muhammet.notepad.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import com.muhammet.notepad.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class BackButtonDialogFragment extends DialogFragment {
    public interface Listener {
        void onBackDialogPositiveClick(String filename);
        void onBackDialogNegativeClick(String filename);
    }

    Listener listener;

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
        builder.setMessage(R.string.dialog_save_changes)
        .setTitle(R.string.dialog_save_button_title)
        .setPositiveButton(R.string.action_save, (dialog, id) -> listener.onBackDialogPositiveClick(getArguments().getString("filename")))
        .setNegativeButton(R.string.action_discard, (dialog, id) -> listener.onBackDialogNegativeClick(getArguments().getString("filename")));
        return builder.create();
    }
}
