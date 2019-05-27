package com.muhammet.notepad.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.FileUriExposedException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.muhammet.notepad.R;
import com.muhammet.notepad.fragment.dialog.FirstViewDialogFragment;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import us.feras.mdv.MarkdownView;

public class NoteViewFragment extends Fragment {
    private MarkdownView markdownView;
    String filename = "";
    String contentsOnLoad = "";
    int firstLoad;
    boolean showMessage = true;

    public class DeleteNotesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] filesToDelete = intent.getStringArrayExtra("files");

            for(Object file : filesToDelete) {
                if(filename.equals(file)) {
                    Fragment fragment;
                    if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                        fragment = new NoteListFragment();
                    else
                        fragment = new WelcomeFragment();

                    getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                }
            }
        }
    }

    IntentFilter filter = new IntentFilter("com.muhammet.notepad.DELETE_NOTES");
    DeleteNotesReceiver receiver = new DeleteNotesReceiver();

    public interface Listener {
        void showDeleteDialog();
        String loadNote(String filename) throws IOException;
        String loadNoteTitle(String filename) throws IOException;
        void exportNote(String filename);
        void printNote(String contentToPrint);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        return inflater.inflate(
                pref.getBoolean("markdown", false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? R.layout.fragment_note_view_md
                        : R.layout.fragment_note_view, container, false);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        filename = getArguments().getString("filename");
        String title;
        try {
            title = listener.loadNoteTitle(filename);
        } catch (IOException e) {
            title = getResources().getString(R.string.view_note);
        }

        getActivity().setTitle(title);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.ic_recents_logo)).getBitmap();

            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(title, bitmap, ContextCompat.getColor(getActivity(), R.color.primary));
            getActivity().setTaskDescription(taskDescription);
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-large")
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LinearLayout noteViewEdit = getActivity().findViewById(R.id.noteViewEdit);
            LinearLayout noteList = getActivity().findViewById(R.id.noteList);

            noteList.animate().z(0f);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                noteViewEdit.animate().z(getResources().getDimensionPixelSize(R.dimen.note_view_edit_elevation_land));
            else
                noteViewEdit.animate().z(getResources().getDimensionPixelSize(R.dimen.note_view_edit_elevation));
        }

        TextView noteContents = getActivity().findViewById(R.id.textView);
        markdownView = getActivity().findViewById(R.id.markdownView);

        SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        ScrollView scrollView = getActivity().findViewById(R.id.scrollView);
        String theme = pref.getString("theme", "light-sans");
        int textSize = -1;
        int textColor = -1;

        String fontFamily = null;

        if(theme.contains("light")) {
            if(noteContents != null) {
                noteContents.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_primary));
                noteContents.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background));
            }

            if(markdownView != null) {
                markdownView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background));
                textColor = ContextCompat.getColor(getActivity(), R.color.text_color_primary);
            }

            scrollView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background));
        }

        if(theme.contains("dark")) {
            if(noteContents != null) {
                noteContents.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_primary_dark));
                noteContents.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background_dark));
            }

            if(markdownView != null) {
                markdownView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background_dark));
                textColor = ContextCompat.getColor(getActivity(), R.color.text_color_primary_dark);
            }

            scrollView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background_dark));
        }

        if(theme.contains("sans")) {
            if(noteContents != null)
                noteContents.setTypeface(Typeface.SANS_SERIF);

            if(markdownView != null)
                fontFamily = "sans-serif";
        }

        if(theme.contains("serif")) {
            if(noteContents != null)
                noteContents.setTypeface(Typeface.SERIF);

            if(markdownView != null)
                fontFamily = "serif";
        }

        if(theme.contains("monospace")) {
            if(noteContents != null)
                noteContents.setTypeface(Typeface.MONOSPACE);

            if(markdownView != null)
                fontFamily = "monospace";
        }

        switch(pref.getString("font_size", "normal")) {
            case "smallest":
                textSize = 12;
                break;
            case "small":
                textSize = 14;
                break;
            case "normal":
                textSize = 16;
                break;
            case "large":
                textSize = 18;
                break;
            case "largest":
                textSize = 20;
                break;
        }

        if(noteContents != null)
            noteContents.setTextSize(textSize);

        String css = "";
        if(markdownView != null) {
            String topBottom = " " + Float.toString(getResources().getDimension(R.dimen.padding_top_bottom) / getResources().getDisplayMetrics().density) + "px";
            String leftRight = " " + Float.toString(getResources().getDimension(R.dimen.padding_left_right) / getResources().getDisplayMetrics().density) + "px";
            String fontSize = " " + Integer.toString(textSize) + "px";
            String fontColor = " #" + StringUtils.remove(Integer.toHexString(textColor), "ff");
            String linkColor = " #" + StringUtils.remove(Integer.toHexString(new TextView(getActivity()).getLinkTextColors().getDefaultColor()), "ff");

            css = "body { " +
                    "margin:" + topBottom + topBottom + leftRight + leftRight + "; " +
                    "font-family:" + fontFamily + "; " +
                    "font-size:" + fontSize + "; " +
                    "color:" + fontColor + "; " +
                    "}" +
                    "a { " +
                    "color:" + linkColor + "; " +
                    "}";

            markdownView.getSettings().setJavaScriptEnabled(false);
            markdownView.getSettings().setLoadsImagesAutomatically(false);
            markdownView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException | FileUriExposedException e) { /* Gracefully fail */ }
                    else
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) { /* Gracefully fail */ }

                    return true;
                }
            });
        }

        // Load note contents
        try {
            contentsOnLoad = listener.loadNote(filename);
        } catch (IOException e) {
            showToast(R.string.error_loading_note);

            Fragment fragment;
            if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                fragment = new NoteListFragment();
            else
                fragment = new WelcomeFragment();

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }

        if(noteContents != null)
            noteContents.setText(contentsOnLoad);

        if(markdownView != null)
            markdownView.loadMarkdown(contentsOnLoad,
                    "data:text/css;base64," + Base64.encodeToString(css.getBytes(), Base64.DEFAULT));

        final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        firstLoad = sharedPref.getInt("first-load", 0);
        if(firstLoad == 0) {
            // Show dialog with info
            DialogFragment firstLoad = new FirstViewDialogFragment();
            firstLoad.show(getFragmentManager(), "firstloadfragment");

            // Set first-load preference to 1; we don't need to show the dialog anymore
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("first-load", 1);
            editor.apply();
        }

        final GestureDetector detector = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {}

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {}

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
        });

        detector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(sharedPref.getBoolean("show_double_tap_message", true)) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("show_double_tap_message", false);
                    editor.apply();
                }

                Bundle bundle = new Bundle();
                bundle.putString("filename", filename);

                Fragment fragment = new NoteEditFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                        .commit();

                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(sharedPref.getBoolean("show_double_tap_message", true) && showMessage) {
                    showToastLong(R.string.double_tap);
                    showMessage = false;
                }

                return false;
            }

        });

        if(noteContents != null)
            noteContents.setOnTouchListener((v, event) -> {
                detector.onTouchEvent(event);
                return false;
            });

        if(markdownView != null)
            markdownView.setOnTouchListener((v, event) -> {
                detector.onTouchEvent(event);
                return false;
            });
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(markdownView != null && markdownView.canGoBack())
            markdownView.goBack();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            case R.id.action_edit:
                Bundle bundle = new Bundle();
                bundle.putString("filename", filename);

                Fragment fragment = new NoteEditFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                        .commit();

                return true;

            case R.id.action_delete:
                listener.showDeleteDialog();
                return true;

            case R.id.action_share:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, contentsOnLoad);
                intent.setType("text/plain");

                if(intent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));

                return true;

            case R.id.action_export:
                listener.exportNote(filename);
                return true;

            case R.id.action_print:
                listener.printNote(contentsOnLoad);
                return true;

            case R.id.action_reminder:

                DialogFragment timePickerFragment=new TimePickerFragment();
                timePickerFragment.setCancelable(false);
                timePickerFragment.show(getFragmentManager(),"timePicker");

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteNote(String filename) {
        File fileToDelete = new File(getActivity().getFilesDir() + File.separator + filename);
        fileToDelete.delete();
    }

    private void showToast(int message) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showToastLong(int message) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_LONG);
        toast.show();
    }

    public void onDeleteDialogPositiveClick() {
        deleteNote(filename);
        showToast(R.string.note_deleted);

        if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-large")) {
            Intent listNotesIntent = new Intent();
            listNotesIntent.setAction("com.muhammet.notepad.LIST_NOTES");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(listNotesIntent);
        }

        Fragment fragment;
        if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
            fragment = new NoteListFragment();
        else
            fragment = new WelcomeFragment();

        getFragmentManager()
            .beginTransaction()
            .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit();
    }

        public void dispatchKeyShortcutEvent(int keyCode) {
            switch(keyCode){

                case KeyEvent.KEYCODE_E:
                    Bundle bundle = new Bundle();
                    bundle.putString("filename", filename);

                    Fragment fragment = new NoteEditFragment();
                    fragment.setArguments(bundle);

                    getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                        .commit();
                    break;

                case KeyEvent.KEYCODE_D:
                    listener.showDeleteDialog();
                    break;

                case KeyEvent.KEYCODE_H:
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, contentsOnLoad);
                    shareIntent.setType("text/plain");

                    if(shareIntent.resolveActivity(getActivity().getPackageManager()) != null)
                        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

                    break;
            }
        }

        public void onBackPressed() {
            Fragment fragment;
            if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                fragment = new NoteListFragment();
            else
                fragment = new WelcomeFragment();

            getFragmentManager()
                .beginTransaction()
                .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        }

    public String getFilename() {
        return getArguments().getString("filename");
    }
}
