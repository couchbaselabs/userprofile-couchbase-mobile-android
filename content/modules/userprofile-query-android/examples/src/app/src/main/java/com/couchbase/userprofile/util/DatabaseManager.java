package com.couchbase.userprofile.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.renderscript.Sampler;
import android.util.Log;

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseChange;
import com.couchbase.lite.DatabaseChangeListener;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.EncryptionKey;
import com.couchbase.lite.Expression;
import com.couchbase.lite.IndexBuilder;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.ValueIndexItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DatabaseManager {
    private static Database userprofileDatabase;
    private static Database universityDatabase;
    private static DatabaseManager instance = null;
    private ListenerToken listenerToken;
    public  String currentUser = null;
    private static String dbName = "userprofile";

    protected DatabaseManager() {

    }

    public static DatabaseManager getSharedInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }

        return instance;
    }

    public static Database getUserProfileDatabase() {
        return userprofileDatabase;
    }
    public static Database getUniversityDatabase() { return universityDatabase; }

    // tag::initCouchbaseLite[]
    public void initCouchbaseLite(Context context) {
        CouchbaseLite.init(context);

    }
    // end::initCouchbaseLite[]

    // tag::userProfileDocId[]
    public String getCurrentUserDocId() {
        return "user::" + currentUser;
    }
    // end::userProfileDocId[]

    // tag::openOrCreateDatabase[]
    public void openOrCreateDatabaseForUser(Context context, String username)
    // end::openOrCreateDatabase[]
    {
        // tag::databaseConfiguration[]
        DatabaseConfiguration config = new DatabaseConfiguration();
        config.setEncryptionKey(new EncryptionKey("PASSWORD"));
        // end::databaseConfiguration[]

        currentUser = username;

        try {
            // tag::createDatabase[]
            userprofileDatabase = new Database(dbName, config);
            // end::createDatabase[]
            registerForDatabaseChanges();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    // tag::openPrebuiltDatabase[]
    public void openPrebuiltDatabase(Context context)
    // end::openPrebuiltDatabase[]
    {
        File dbFile = new File(context.getFilesDir(), "universities.cblite2");

        // tag::prebuiltdbconfig[]
        DatabaseConfiguration config = new DatabaseConfiguration();
        config.setDirectory(context.getFilesDir().toString());
        // end::prebuiltdbconfig[]

        Log.i("CB-Update", "Will open Prebuilt DB  at path " + config.getDirectory());

        // tag::prebuiltdbopen[]
        if (!dbFile.exists()) {
            AssetManager assetManager = context.getAssets();
            try {
                File path = new File(context.getFilesDir().toString());

                unzip(assetManager.open("universities.zip"), path);

                universityDatabase = new Database("universities", config);
                createUniversityDatabaseIndexes();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                universityDatabase = new Database("universities", config);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        // end::prebuiltdbopen[]
    }

    // tag::createUniversityDatabaseIndexes[]
    private void createUniversityDatabaseIndexes() {
        try {
            universityDatabase.createIndex("nameLocationIndex", IndexBuilder.valueIndex(ValueIndexItem.expression(Expression.property("name")),
                    ValueIndexItem.expression(Expression.property("location"))));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
    // end::createUniversityDatabaseIndexes[]

    // tag::registerForDatabaseChanges[]
    private void registerForDatabaseChanges()
    // end::registerForDatabaseChanges[]
    {
        // tag::addDatabaseChangelistener[]
        // Add database change listener
        listenerToken = userprofileDatabase.addChangeListener(new DatabaseChangeListener() {
            @Override
            public void changed(final DatabaseChange change) {
                if (change != null) {
                    for(String docId : change.getDocumentIDs()) {
                        Document doc = userprofileDatabase.getDocument(docId);
                        if (doc != null) {
                            Log.i("DatabaseChangeEvent", "Document was added/updated");
                        }
                        else {

                            Log.i("DatabaseChangeEvent","Document was deleted");
                        }
                    }
                }
            }
        });
        // end::addDatabaseChangelistener[]
    }

    public void closeDatabaseForUser()
    {
        try {
            if (userprofileDatabase != null) {
                deregisterForDatabaseChanges();
                userprofileDatabase.close();
                userprofileDatabase = null;
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    // tag::closePrebuiltDatabase[]
    public void closePrebuiltDatabase()
    // end::closePrebuiltDatabase[]
    {
        try {
            if (userprofileDatabase != null) {
                deregisterForDatabaseChanges();
                // tag::prebuiltdbclose[]
                userprofileDatabase.close();
                // end::prebuiltdbclose[]
                userprofileDatabase = null;
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    // tag::deregisterForDatabaseChanges[]
    private void deregisterForDatabaseChanges()
    // end::deregisterForDatabaseChanges[]
    {
        if (listenerToken != null) {
            // tag::removedbchangelistener[]
            userprofileDatabase.removeChangeListener(listenerToken);
            // end::removedbchangelistener[]
        }
    }

    private static void unzip(InputStream in, File destination) throws IOException {
        byte[] buffer = new byte[1024];

        ZipInputStream zis = new ZipInputStream(in);
        ZipEntry ze = zis.getNextEntry();

        while (ze != null) {
            String fileName = ze.getName();
            File newFile = new File(destination, fileName);

            if (ze.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;

                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
            }

            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

        in.close();
    }
}
