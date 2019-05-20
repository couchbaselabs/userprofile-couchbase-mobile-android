package com.couchbase.userprofile.util;

import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;

public class DatabaseManager {
    private static Database database;
    private static DatabaseManager instance = null;
    public  String currentUser = null;
    private static String dbName = "userprofiles";

    protected DatabaseManager() {

    }

    public static DatabaseManager getSharedInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }

        return instance;
    }

    public static Database getDatabase() {
        return database;
    }

    public String getCurrentUserDocId() {
        return "user::" + currentUser;
    }

    // TODO: Should I even include password here?
    public void OpenOrCreateDatabaseForUser(Context context, String username) {
        DatabaseConfiguration config = new DatabaseConfiguration(context);
        config.setDirectory(String.format("%s/%s", context.getFilesDir(), username));
        currentUser = username;

        try {
            database = new Database(dbName, config);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void CloseDatabase() {
        try {
            database.close();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
