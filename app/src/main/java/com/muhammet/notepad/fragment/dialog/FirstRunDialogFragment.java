package com.muhammet.notepad.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import com.muhammet.notepad.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FirstRunDialogFragment extends DialogFragment {

    public interface Listener {
        void onFirstRunDialogPositiveClick();
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
        builder.setMessage(R.string.first_run)
        .setTitle(R.string.app_name)
        .setPositiveButton(R.string.action_close, (dialog, id) -> listener.onFirstRunDialogPositiveClick());

        setCancelable(false);

        return builder.create();
    }
}