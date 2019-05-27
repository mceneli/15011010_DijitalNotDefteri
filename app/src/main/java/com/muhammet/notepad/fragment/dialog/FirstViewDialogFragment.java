package com.muhammet.notepad.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;

import com.muhammet.notepad.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FirstViewDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.first_view)
        .setTitle(R.string.app_name)
        .setPositiveButton(R.string.action_close, null);

        return builder.create();
    }
}