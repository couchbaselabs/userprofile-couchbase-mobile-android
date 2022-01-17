package com.couchbase.userprofile.profile;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Blob;
import com.couchbase.lite.Database;
import com.couchbase.userprofile.R;
import com.couchbase.userprofile.login.LoginActivity;
import com.couchbase.userprofile.universities.UniversitiesActivity;
import com.couchbase.userprofile.util.DatabaseManager;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity implements UserProfileContract.View {

    static final int PICK_UNIVERSITY = 2;

    private UserProfileContract.UserActionsListener mActionListener;

    EditText nameInput;
    EditText emailInput;
    EditText addressInput;
    TextView universityText;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        addressInput = findViewById(R.id.addressInput);
        universityText = findViewById(R.id.universityText);
        imageView = findViewById(R.id.imageView);

        mActionListener = new UserProfilePresenter(this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActionListener.fetchProfile();
            }
        });
    }

    public static final int PICK_IMAGE = 1;

    public void onUploadPhotoTapped(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException ex) {
                    Log.i("SelectPhoto", ex.getMessage());
                }
            }
        }
        else if (requestCode == PICK_UNIVERSITY && resultCode == Activity.RESULT_OK) {
            universityText.setText(data.getStringExtra("result"));
        }
    }

    public void onUniversityTapped(View view) {
        Intent intent = new Intent(getApplicationContext(), UniversitiesActivity.class);
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, PICK_UNIVERSITY);
    }

    public void onLogoutTapped(View view) {
        DatabaseManager.stopAllReplicationForCurrentUser();
        DatabaseManager.getSharedInstance().closePrebuiltDatabase();
        DatabaseManager.getSharedInstance().closeDatabaseForUser();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onSaveTapped(View view) {
        // tag::userprofile[]
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", nameInput.getText().toString());
        profile.put("email", emailInput.getText().toString());
        profile.put("address", addressInput.getText().toString());
        profile.put("university", universityText.getText().toString());
        profile.put("type", "user");
        byte[] imageViewBytes = getImageViewBytes();

        if (imageViewBytes != null) {
            profile.put("imageData", new com.couchbase.lite.Blob("image/jpeg", imageViewBytes));
        }
        // end::userprofile[]

        mActionListener.saveProfile(profile);

        Toast.makeText(this, "Successfully updated profile!", Toast.LENGTH_SHORT).show();
    }

    private byte[] getImageViewBytes() {

        byte[] imageBytes = null;

        if (checkPermissions("PICK_FROM_GALLERY")) {

            BitmapDrawable bmDrawable = (BitmapDrawable) imageView.getDrawable();

            if (bmDrawable != null) {
                Bitmap bitmap = bmDrawable.getBitmap();

                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    imageBytes = baos.toByteArray();
                }
            }
        }
        return imageBytes;

    }

    private boolean checkPermissions(String permission) {
        // Function to check and request permission
        int requestCode = 0;
        boolean result = false;
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            if (requestCode == PERMISSION_GRANTED) {
                result = true;
            }
        }
        else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            result = true;
        }
        return result;
    }


    @Override
    public void showProfile(Map<String, Object> profile) {
        nameInput.setText((String)profile.get("name"));
        emailInput.setText((String)profile.get("email"));
        addressInput.setText((String)profile.get("address"));

        String university = (String)profile.get("university");

        if (university != null && !university.isEmpty()) {
            universityText.setText(university);
        }

        Blob imageBlob = (Blob)profile.get("imageData");

        if (imageBlob != null) {
            Drawable d = Drawable.createFromStream(imageBlob.getContentStream(), "res");
            imageView.setImageDrawable(d);
        }
    }
}
