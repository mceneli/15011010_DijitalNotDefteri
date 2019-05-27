package com.muhammet.notepad.util;

import java.text.Collator;
import java.util.Comparator;

public class NoteListItem {
    private String note;
    private String date;

    public NoteListItem(String note, String date) {
        this.note = note;
        this.date = date;
    }

    public String getNote() {
      return note;
    }

    public String getDate() {
        return date;
    }

    public static Comparator<NoteListItem> NoteComparatorTitle = (arg1, arg2) -> Collator.getInstance().compare(arg1.getNote(), arg2.getNote());
}
