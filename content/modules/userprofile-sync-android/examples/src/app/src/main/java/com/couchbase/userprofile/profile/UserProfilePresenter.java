package com.couchbase.userprofile.profile;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.userprofile.util.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

public class UserProfilePresenter implements UserProfileContract.UserActionsListener {
    private UserProfileContract.View mUserProfileView;

    public UserProfilePresenter(UserProfileContract.View mUserProfileView) {
        this.mUserProfileView = mUserProfileView;
    }

    // tag::fetchProfile[]
    public void fetchProfile()
    // end::fetchProfile[]
    {
        Database database = DatabaseManager.getUserProfileDatabase();

        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();

        // tag::livequerybuilder[]
        Query query = QueryBuilder
                        .select(SelectResult.all())
                        .from(DataSource.database(database))
                        .where(Meta.id.equalTo(Expression.string(docId))); // <1>
        // end::livequerybuilder[]

        // tag::livequery[]
        query.addChangeListener(new QueryChangeListener() {

            @Override
            public void changed(QueryChange change) { // <1>
                ResultSet rows = change.getResults();

                Result row = null;
                Map<String, Object> profile = new HashMap<>(); // <2>

                profile.put("email", DatabaseManager.getSharedInstance().currentUser);

                while ((row = rows.next()) != null) {
                    Dictionary dictionary = row.getDictionary("userprofile"); // <3>

                    if (dictionary != null) {
                        profile.put("name", dictionary.getString("name")); // <4>
                        profile.put("address", dictionary.getString("address")); // <4>
                        profile.put("imageData", dictionary.getBlob("imageData")); // <4>
                        profile.put("university", dictionary.getString("university")); // <4>
                        profile.put("type", dictionary.getString("type")); // <4>
                    }
                }

                mUserProfileView.showProfile(profile);
            }
        });
        // end::livequery[]

        try {
            query.execute();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void saveProfile(Map<String,Object> profile)
    {
        Database database = DatabaseManager.getUserProfileDatabase();
        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();
        MutableDocument mutableDocument = new MutableDocument(docId, profile);

        try {
            database.save(mutableDocument);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
