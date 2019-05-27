package com.muhammet.notepad.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.muhammet.notepad.activity.MainActivity;
import com.muhammet.notepad.R;
import com.muhammet.notepad.activity.SettingsActivity;
import com.muhammet.notepad.adapter.NoteListAdapter;
import com.muhammet.notepad.adapter.NoteListDateAdapter;
import com.muhammet.notepad.fragment.dialog.AboutDialogFragment;
import com.muhammet.notepad.util.NoteListItem;
import com.muhammet.notepad.util.ScrollPositions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NoteListFragment extends Fragment {
    String theme;
    String sortBy;
    boolean showDate = true;
    boolean directEdit = false;

    private ListView listView;

    public class ListNotesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            listNotes();
        }
    }

    IntentFilter filter = new IntentFilter("com.muhammet.notepad.LIST_NOTES");
    ListNotesReceiver receiver = new ListNotesReceiver();

    public interface Listener {
        void viewNote(String filename);
        void editNote(String filename);
        String getCabString(int size);
        void exportNotes();
        void deleteNotes();
        String loadNoteTitle(String filename) throws IOException;
        String loadNoteDate(String filename);
        void showFab();
        void hideFab();
        void startMultiSelect();
        ArrayList<String> getCabArray();
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefMain = getActivity().getPreferences(Context.MODE_PRIVATE);
        if(getId() == R.id.noteViewEdit && prefMain.getLong("draft-name", 0) != 0) {
            Bundle bundle = new Bundle();
            bundle.putString("filename", "draft");

            Fragment fragment = new NoteEditFragment();
            fragment.setArguments(bundle);

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        } else {
            if(getId() == R.id.noteViewEdit) {
                String title = getResources().getString(R.string.app_name);

                getActivity().setTitle(title);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.ic_recents_logo)).getBitmap();

                    ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(title, bitmap, ContextCompat.getColor(getActivity(), R.color.primary));
                    getActivity().setTaskDescription(taskDescription);
                }

                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
            }

            SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
            theme = pref.getString("theme", "light-sans");
            sortBy = pref.getString("sort_by", "date");
            showDate = pref.getBoolean("show_date", true);
            directEdit = pref.getBoolean("direct_edit", false);

            LinearLayout noteViewEdit = getActivity().findViewById(R.id.noteViewEdit);
            LinearLayout noteList = getActivity().findViewById(R.id.noteList);

            if(theme.contains("light")) {
                noteViewEdit.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background));
                noteList.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background));
            }

            if(theme.contains("dark")) {
                noteViewEdit.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background_dark));
                noteList.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background_dark));
            }
            listView = getActivity().findViewById(R.id.listView1);
            listNotes();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);

        FloatingActionButton floatingActionButton = getActivity().findViewById(R.id.button_floating_action);
        floatingActionButton.setImageResource(R.drawable.ic_action_new);
        if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-large"))
            floatingActionButton.hide();

        SharedPreferences prefMain = getActivity().getPreferences(Context.MODE_PRIVATE);

        if(getId() == R.id.noteViewEdit && prefMain.getLong("draft-name", 0) == 0) {
            floatingActionButton.show();
            floatingActionButton.setOnClickListener(v -> {
                ScrollPositions.getInstance().setPosition(listView.getFirstVisiblePosition());
                listener.getCabArray().clear();

                Bundle bundle = new Bundle();
                bundle.putString("filename", "new");

                Fragment fragment = new NoteEditFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(getId() == R.id.noteViewEdit)
            inflater.inflate(R.menu.main, menu);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch(item.getItemId()) {
            case R.id.action_start_selection:
                listener.startMultiSelect();
                return true;
            case R.id.action_settings:
                Intent intentSettings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_import:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"text/plain", "text/html", "text/x-markdown"});
                intent.setType("*/*");

                try {
                    getActivity().startActivityForResult(intent, MainActivity.IMPORT);
                } catch (ActivityNotFoundException e) {
                    showToast(R.string.error_importing_notes);
                }
                return true;
            case R.id.action_about:
                DialogFragment aboutFragment = new AboutDialogFragment();
                aboutFragment.show(getFragmentManager(), "about");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startMultiSelect() {
        if(listView.getAdapter().getCount() > 0)
            listView.setItemChecked(-1, true);
        else
            showToast(R.string.no_notes_to_select);
    }

    private void listNotes() {
        String[] listOfFiles = getActivity().getFilesDir().list();
        ArrayList<String> listOfNotes = new ArrayList<>();

        int numOfNotes = listOfFiles.length;

        for(String listOfFile : listOfFiles) {
            if(NumberUtils.isCreatable(listOfFile))
                listOfNotes.add(listOfFile);
            else
                numOfNotes--;
        }

        String[] listOfNotesByDate = new String[numOfNotes];
        String[] listOfNotesByName = new String[numOfNotes];

        NoteListItem[] listOfTitlesByDate = new NoteListItem[numOfNotes];
        NoteListItem[] listOfTitlesByName = new NoteListItem[numOfNotes];

        ArrayList<NoteListItem> list = new ArrayList<>(numOfNotes);

        for(int i = 0; i < numOfNotes; i++) {
            listOfNotesByDate[i] = listOfNotes.get(i);
        }

        if(sortBy.startsWith("date")) {
            Arrays.sort(listOfNotesByDate, Collections.reverseOrder());
            if(sortBy.endsWith("reversed"))
                ArrayUtils.reverse(listOfNotesByDate);
        }

        for(int i = 0; i < numOfNotes; i++) {
            try {
                String title = listener.loadNoteTitle(listOfNotesByDate[i]);
                String date = listener.loadNoteDate(listOfNotesByDate[i]);
                listOfTitlesByDate[i] = new NoteListItem(title, date);
            } catch (IOException e) {
                showToast(R.string.error_loading_list);
            }
        }

        if(sortBy.startsWith("name")) {
            System.arraycopy(listOfTitlesByDate, 0, listOfTitlesByName, 0, numOfNotes);
            Arrays.sort(listOfTitlesByName, NoteListItem.NoteComparatorTitle);
            if(sortBy.endsWith("reversed"))
                ArrayUtils.reverse(listOfTitlesByName);

            for(int i = 0; i < numOfNotes; i++)
                listOfNotesByName[i] = "new";

            for(int i = 0; i < numOfNotes; i++) {
                for(int j = 0; j < numOfNotes; j++) {
                    if(listOfTitlesByName[i].getNote().equals(listOfTitlesByDate[j].getNote())
                            && listOfNotesByName[i].equals("new")) {
                        listOfNotesByName[i] = listOfNotesByDate[j];
                        listOfNotesByDate[j] = "";
                        listOfTitlesByDate[j] = new NoteListItem("", "");
                    }
                }
            }

            list.addAll(Arrays.asList(listOfTitlesByName));
        } else if(sortBy.startsWith("date"))
            list.addAll(Arrays.asList(listOfTitlesByDate));

        final NoteListDateAdapter dateAdapter = new NoteListDateAdapter(getActivity(), list);
        final NoteListAdapter adapter = new NoteListAdapter(getActivity(), list);

        if(showDate)
            listView.setAdapter(dateAdapter);
        else
            listView.setAdapter(adapter);

        listView.setSelection(ScrollPositions.getInstance().getPosition());

        final String[] finalListByDate = listOfNotesByDate;
        final String[] finalListByName = listOfNotesByName;

        listView.setClickable(true);
        listView.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            ScrollPositions.getInstance().setPosition(listView.getFirstVisiblePosition());

            if(sortBy.startsWith("date")) {
                if(directEdit)
                    listener.editNote(finalListByDate[position]);
                else
                    listener.viewNote(finalListByDate[position]);
            } else if(sortBy.startsWith("name")) {
                if(directEdit)
                    listener.editNote(finalListByName[position]);
                else
                    listener.viewNote(finalListByName[position]);
            }
        });

        final ArrayList<String> cab = listener.getCabArray();

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.action_select_all:
                        cab.clear();

                        for(int i = 0; i < listView.getAdapter().getCount(); i++) {
                            listView.setItemChecked(i, true);
                        }
                        return false;
                    case R.id.action_export:
                        if(cab.size() > 0) {
                            mode.finish();
                            listener.exportNotes();
                            return true;
                        } else {
                            showToast(R.string.no_notes_to_export);
                            return false;
                        }
                    case R.id.action_delete:
                        if(cab.size() > 0) {
                            mode.finish();
                            listener.deleteNotes();
                            return true;
                        } else {
                            showToast(R.string.no_notes_to_delete);
                            return false;
                        }
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                listener.hideFab();
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                cab.clear();

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                listener.showFab();
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if(position > -1) {
                    if(checked) {
                        if(sortBy.startsWith("date"))
                            cab.add(finalListByDate[position]);
                        if(sortBy.startsWith("name"))
                            cab.add(finalListByName[position]);
                    } else {
                        if(sortBy.startsWith("date"))
                            cab.remove(finalListByDate[position]);
                        if(sortBy.startsWith("name"))
                            cab.remove(finalListByName[position]);
                    }

                    listView.setItemChecked(-1, false);
                }
                mode.setTitle(cab.size() + " " + listener.getCabString(cab.size()));
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        if(cab.size() > 0) {
            List<String> cabClone = new ArrayList<>(cab);
            cab.clear();

            String[] array = null;
            if(sortBy.startsWith("date"))
                array = finalListByDate;
            if(sortBy.startsWith("name"))
                array = finalListByName;

            if(array != null) {
                for(String filename : cabClone) {
                    for(int i = 0; i < array.length; i++) {
                        if(filename.equals(array[i]))
                            listView.setItemChecked(i, true);
                    }
                }
            }
        }

        if(numOfNotes == 0) {
            TextView empty = getActivity().findViewById(R.id.empty);
            listView.setEmptyView(empty);
        }
    }

    // Method used to generate toast notifications
    private void showToast(int message) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

    public void dispatchKeyShortcutEvent(int keyCode) {
        if(getId() == R.id.noteViewEdit) {
            switch(keyCode) {
                // CTRL+N: New Note
                case KeyEvent.KEYCODE_N:
                    ScrollPositions.getInstance().setPosition(listView.getFirstVisiblePosition());

                    Bundle bundle = new Bundle();
                    bundle.putString("filename", "new");

                    Fragment fragment = new NoteEditFragment();
                    fragment.setArguments(bundle);

                    // Add NoteEditFragment
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .commit();
                    break;
            }
        }
    }

    public void onBackPressed() {
        getActivity().finish();
    }

    public void showFab() {
        FloatingActionButton floatingActionButton = getActivity().findViewById(R.id.button_floating_action);
        floatingActionButton.show();
    }

    public void hideFab() {
        FloatingActionButton floatingActionButton = getActivity().findViewById(R.id.button_floating_action);
        floatingActionButton.hide();
    }
}