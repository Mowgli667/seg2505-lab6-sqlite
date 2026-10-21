package com.example.sqlitebasics;

import android.provider.BaseColumns;

public final class FeedReaderContract {
    // Constructeur privé : on n'instancie jamais cette classe
    private FeedReaderContract() {}

    // Une classe interne par table
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
    }
}
