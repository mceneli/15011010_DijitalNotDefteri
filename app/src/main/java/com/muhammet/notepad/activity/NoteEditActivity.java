package com.muhammet.notepad.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.muhammet.notepad.R;
import com.muhammet.notepad.fragment.NoteEditFragment;
import com.muhammet.notepad.fragment.dialog.BackButtonDialogFragment;
import com.muhammet.notepad.fragment.dialog.DeleteDialogFragment;
import com.muhammet.notepad.fragment.dialog.SaveButtonDialogFragment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class NoteEditActivity extends NotepadBaseActivity implements
BackButtonDialogFragment.Listener, 
DeleteDialogFragment.Listener, 
SaveButtonDialogFragment.Listener, 
NoteEditFragment.Listener {

String external;

    @Override
    public boolean isShareIntent() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String theme = pref.getString("theme", "light-sans");

        LinearLayout noteViewEdit = findViewById(R.id.noteViewEdit);

        if(theme.contains("light"))
            noteViewEdit.setBackgroundColor(ContextCompat.getColor(this, R.color.window_background));

        if(theme.contains("dark"))
            noteViewEdit.setBackgroundColor(ContextCompat.getColor(this, R.color.window_background_dark));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.action_bar_elevation));

        if(!(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment)) {
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            if(Intent.ACTION_SEND.equals(action) && type != null) {
                if("text/plain".equals(type)) {
                    external = getExternalContent();
                    if(external != null) {
                        newNote();
                    } else {
                        showToast(R.string.loading_external_file);
                        finish();
                    }
                } else {
                    showToast(R.string.loading_external_file);
                    finish();
                }

            } else if("com.google.android.gm.action.AUTO_SEND".equals(action) && type != null) {
                if("text/plain".equals(type)) {
                    external = getExternalContent();
                    if(external != null) {
                        try {
                            FileOutputStream output = openFileOutput(String.valueOf(System.currentTimeMillis()), Context.MODE_PRIVATE);
                            output.write(external.getBytes());
                            output.close();

                            showToast(R.string.note_saved);
                            finish();
                        } catch (IOException e) {
                            showToast(R.string.failed_to_save);
                            finish();
                        }
                    }
                }
            } else if(Intent.ACTION_EDIT.equals(action) && "text/plain".equals(type)) {
                external = intent.getStringExtra(Intent.EXTRA_TEXT);
                if(external != null) {
                    newNote();
                    return;
                }
                finish();
            } else if(Intent.ACTION_VIEW.equals(action) && "text/plain".equals(type)) {
                try{
                    InputStream in = getContentResolver().openInputStream(intent.getData());
                    Reader rd = new InputStreamReader(in, "UTF-8");
                    char[] buffer = new char[4096];
                    int len;
                    StringBuilder sb = new StringBuilder();
                    while ((len = rd.read(buffer)) != -1) {
                        sb.append(buffer, 0, len);
                    }
                    rd.close();
                    in.close();
                    external= sb.toString();
                } catch(Exception e) {
                }
                if(external != null) {
                    newNote();
                    return;
                }
                finish();
            } else
                newNote();
        }
    }

    private String getExternalContent() {
        String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if(text == null) return null;

        String subject = getIntent().getStringExtra(Intent.EXTRA_SUBJECT);
        if(subject == null) return text;

        return subject + "\n\n" + text;
    }

    private void newNote() {
        Bundle bundle = new Bundle();
        bundle.putString("filename", "new");

        Fragment fragment = new NoteEditFragment();
        fragment.setArguments(bundle);

        // Add NoteEditFragment
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.noteViewEdit, fragment, "NoteEditFragment")
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(external != null) {
            EditText noteContents = findViewById(R.id.editText1);
            noteContents.setText(external);
            noteContents.setSelection(external.length(), external.length());
        }
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        super.dispatchKeyShortcutEvent(event);
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed()) {
            NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
            fragment.dispatchKeyShortcutEvent(event.getKeyCode());

            return true;
        }
        return super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public void onBackPressed() {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackPressed(null);
    }

    @Override
    public void onBackDialogNegativeClick(String filename) {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackDialogNegativeClick(null);
    }

    @Override
    public void onBackDialogPositiveClick(String filename) {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackDialogPositiveClick(null);
    }

    @Override
    public void onDeleteDialogPositiveClick() {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onDeleteDialogPositiveClick();
    }

    @Override
    public void onSaveDialogNegativeClick() {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onSaveDialogNegativeClick();
    }

    @Override
    public void onSaveDialogPositiveClick() {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onSaveDialogPositiveClick();
    }

    @Override
    public void showBackButtonDialog(String filename) {
        Bundle bundle = new Bundle();
        bundle.putString("filename", filename);

        DialogFragment backFragment = new BackButtonDialogFragment();
        backFragment.setArguments(bundle);
        backFragment.show(getSupportFragmentManager(), "back");
    }

    @Override
    public void showDeleteDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt("dialog_title", R.string.dialog_delete_button_title);

        DialogFragment deleteFragment = new DeleteDialogFragment();
        deleteFragment.setArguments(bundle);
        deleteFragment.show(getSupportFragmentManager(), "delete");
    }

    @Override
    public void showSaveButtonDialog() {
        DialogFragment saveFragment = new SaveButtonDialogFragment();
        saveFragment.show(getSupportFragmentManager(), "save");
    }

    private void showToast(int message) {
        Toast toast = Toast.makeText(this, getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public String loadNote(String filename) {
        return null;
    }

    @Override
    public String loadNoteTitle(String filename) {
        return null;
    }

    @Override
    public void exportNote(String filename) {}

    @Override
    public void printNote(String contentToPrint) {}
}
