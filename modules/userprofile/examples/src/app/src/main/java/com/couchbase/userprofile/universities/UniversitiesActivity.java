package com.couchbase.userprofile.universities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.couchbase.userprofile.R;
import com.couchbase.userprofile.profile.UserProfileContract;
import com.couchbase.userprofile.profile.UserProfilePresenter;
import com.couchbase.userprofile.util.UniversitiesAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UniversitiesActivity extends AppCompatActivity implements UniversitiesContract.View {

    private UniversitiesContract.UserActionsListener mActionListener;

    private RecyclerView mRecyclerView;
    private SearchView mNameSearchView;
    private SearchView mCountrySearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_universities);

        mNameSearchView = findViewById(R.id.nameSearchView);
        mCountrySearchView = findViewById(R.id.countrySearchView);

        mRecyclerView = findViewById(R.id.universityList);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setLayoutManager(layoutManager);

        UniversitiesAdapter mUniversityAdapter = new UniversitiesAdapter(new ArrayList<Map<String, Object>>());
        mRecyclerView.setAdapter(mUniversityAdapter);

        mActionListener = new UniversitiesPresenter(this);
    }

    public void onLookupTapped(View view) {
        if (mNameSearchView.getQuery().length() > 0) {
            if (mCountrySearchView.getQuery().length() > 0) {
                mActionListener.fetchUniversities(mNameSearchView.getQuery().toString(), mCountrySearchView.getQuery().toString());
            }
            else {
                mActionListener.fetchUniversities(mNameSearchView.getQuery().toString());
            }
        }
    }

    public void showUniversities(final List<Map<String, Object>> universities) {
        UniversitiesAdapter adapter = new UniversitiesAdapter(universities);

        adapter.setOnItemClickListener(new UniversitiesAdapter.OnItemListener() {
            @Override
            public void OnClick(View view, int position) {
                String selectedUniversity = universities.get(position).get("name").toString();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", selectedUniversity);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.invalidate();
    }
}
