package com.muhammet.notepad.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.muhammet.notepad.R;
import com.muhammet.notepad.util.NoteListItem;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;

public class NoteListAdapter extends ArrayAdapter<NoteListItem> {
    public NoteListAdapter(Context context, ArrayList<NoteListItem> notes) {
        super(context, R.layout.row_layout, notes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NoteListItem item = getItem(position);
        String note = item.getNote();

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout, parent, false);

        TextView noteTitle = convertView.findViewById(R.id.noteTitle);
        noteTitle.setText(note);

        SharedPreferences pref = getContext().getSharedPreferences(getContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String theme = pref.getString("theme", "light-sans");

        if(theme.contains("light"))
            noteTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_primary));

        if(theme.contains("dark"))
            noteTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_primary_dark));

        if(theme.contains("sans"))
            noteTitle.setTypeface(Typeface.SANS_SERIF);

        if(theme.contains("serif"))
            noteTitle.setTypeface(Typeface.SERIF);

        if(theme.contains("monospace"))
            noteTitle.setTypeface(Typeface.MONOSPACE);

        switch(pref.getString("font_size", "normal")) {
            case "smallest":
                noteTitle.setTextSize(12);
                break;
            case "small":
                noteTitle.setTextSize(14);
                break;
            case "normal":
                noteTitle.setTextSize(16);
                break;
            case "large":
                noteTitle.setTextSize(18);
                break;
            case "largest":
                noteTitle.setTextSize(20);
                break;
        }

        return convertView;
    }
}