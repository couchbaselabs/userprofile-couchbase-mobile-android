package com.couchbase.userprofile.universities;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Function;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.Result;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.Where;
import com.couchbase.userprofile.util.DatabaseManager;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniversitiesPresenter implements UniversitiesContract.UserActionsListener {
    private UniversitiesContract.View mUniversitiesView;

    public UniversitiesPresenter(UniversitiesContract.View mUniversitiesView) {
        this.mUniversitiesView = mUniversitiesView;
    }

    // tag::fetchUniversities[]
    public void fetchUniversities(String name) {
        fetchUniversities(name, null);
    }

    public void fetchUniversities(String name, String country)
    // end::fetchUniversities[]
    {
        Database database = DatabaseManager.getUniversityDatabase();

        // tag::buildquery[]
        Expression whereQueryExpression = Function.lower(Expression.property("name")).like(Expression.string("%" + name.toLowerCase() + "%")); // <1>

        if (country != null && !country.isEmpty()) {
            Expression countryQueryExpression = Function.lower(Expression.property("country")).like(Expression.string("%" + country.toLowerCase() + "%")); // <2>

            whereQueryExpression = whereQueryExpression.and(countryQueryExpression); // <3>
        }

        Query query = QueryBuilder.select(SelectResult.all()) // <4>
                                  .from(DataSource.database(database)) // <5>
                                  .where(whereQueryExpression); // <6>
        // end::buildquery[]

        ResultSet rows = null;

        // tag::runquery[]
        try {
            rows = query.execute();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return;
        }

        List<Map<String, Object>> data = new ArrayList<>();

        Result row;

        while((row = rows.next()) != null) {
            // tag::university[]
            Map<String, Object> properties = new HashMap<>(); // <1>
            properties.put("name", row.getDictionary("universities").getString("name")); // <2>
            properties.put("country", row.getDictionary("universities").getString("country")); // <2>
            properties.put("web_pages", row.getDictionary("universities").getArray("web_pages")); // <3>
            // end::university[]

            data.add(properties);
        }
        // end::runquery[]

        mUniversitiesView.showUniversities(data);
    }
}
