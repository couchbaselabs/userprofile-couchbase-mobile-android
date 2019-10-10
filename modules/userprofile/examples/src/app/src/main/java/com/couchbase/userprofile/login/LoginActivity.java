package com.couchbase.userprofile.login;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.couchbase.userprofile.R;
import com.couchbase.userprofile.profile.UserProfileActivity;
import com.couchbase.userprofile.util.DatabaseManager;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput;
    EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
    }

    public void onLoginTapped(View view) {
        if (usernameInput.length() > 0 && passwordInput.length() > 0) {
            DatabaseManager dbMgr = DatabaseManager.getSharedInstance();

            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            Context context = getApplicationContext();
            dbMgr.initializeCouchbaseLite(context);
            dbMgr.openPrebuiltDatabase(context);
            dbMgr.openOrCreateDatabaseForUser(context,username);

            DatabaseManager.startPushAndPullReplicationForCurrentUser(username, password);

            Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
