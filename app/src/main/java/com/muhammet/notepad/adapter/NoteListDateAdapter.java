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

public class NoteListDateAdapter extends ArrayAdapter<NoteListItem> {
    public NoteListDateAdapter(Context context, ArrayList<NoteListItem> notes) {
        super(context, R.layout.row_layout_date, notes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NoteListItem item = getItem(position);
        String note = item.getNote();
        String date = item.getDate();

        if(convertView == null)
           convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_date, parent, false);

        TextView noteTitle = convertView.findViewById(R.id.noteTitle);
        TextView noteDate = convertView.findViewById(R.id.noteDate);

        noteTitle.setText(note);
        noteDate.setText(date);

        SharedPreferences pref = getContext().getSharedPreferences(getContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String theme = pref.getString("theme", "light-sans");

        if(theme.contains("light")) {
            noteTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_primary));
            noteDate.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_secondary));
        }

        if(theme.contains("dark")) {
            noteTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_primary_dark));
            noteDate.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_secondary_dark));
        }

        if(theme.contains("sans")) {
            noteTitle.setTypeface(Typeface.SANS_SERIF);
            noteDate.setTypeface(Typeface.SANS_SERIF);
        }

        if(theme.contains("serif")) {
            noteTitle.setTypeface(Typeface.SERIF);
            noteDate.setTypeface(Typeface.SERIF);
        }

        if(theme.contains("monospace")) {
            noteTitle.setTypeface(Typeface.MONOSPACE);
            noteDate.setTypeface(Typeface.MONOSPACE);
        }

        switch(pref.getString("font_size", "normal")) {
            case "smallest":
                noteTitle.setTextSize(12);
                noteDate.setTextSize(8);
                break;
            case "small":
                noteTitle.setTextSize(14);
                noteDate.setTextSize(10);
                break;
            case "normal":
                noteTitle.setTextSize(16);
                noteDate.setTextSize(12);
                break;
            case "large":
                noteTitle.setTextSize(18);
                noteDate.setTextSize(14);
                break;
            case "largest":
                noteTitle.setTextSize(20);
                noteDate.setTextSize(16);
                break;
        }

        return convertView;
    }
}