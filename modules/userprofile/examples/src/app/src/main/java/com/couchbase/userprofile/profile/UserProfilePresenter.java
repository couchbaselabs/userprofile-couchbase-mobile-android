package com.couchbase.userprofile.profile;

import android.provider.ContactsContract;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.userprofile.util.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

public class UserProfilePresenter implements UserProfileContract.UserActionsListener {
    private UserProfileContract.View mUserProfileView;

    public UserProfilePresenter(UserProfileContract.View mUserProfileView) {
        this.mUserProfileView = mUserProfileView;
    }

    public void fetchProfile() {
        Database database = DatabaseManager.getDatabase();

        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();

        if (database != null) {
            Document document = database.getDocument(docId);

            Map<String, Object> profile = new HashMap<>();

            if (document != null) {
                profile.put("name", document.getString("name"));
                profile.put("email", document.getString("email"));
                profile.put("address", document.getString("address"));
                profile.put("imageData", document.getBlob("imageData"));
            }
            else
            {
                profile.put("email", DatabaseManager.getSharedInstance().currentUser);
            }

            mUserProfileView.showProfile(profile);
        }
    }

    public void saveProfile(Map<String,Object> profile) {
        Database database = DatabaseManager.getDatabase();

        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();

        MutableDocument mutableDocument = new MutableDocument(docId, profile);

        try {
            database.save(mutableDocument);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
