package com.couchbase.userprofile.universities;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Function;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.Result;
import com.couchbase.lite.SelectResult;
import com.couchbase.userprofile.util.DatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniversitiesPresenter implements UniversitiesContract.UserActionsListener {
    private UniversitiesContract.View mUniversitiesView;

    public UniversitiesPresenter(UniversitiesContract.View mUniversitiesView) {
        this.mUniversitiesView = mUniversitiesView;
    }

    public void fetchUniversities(String name) {
        fetchUniversities(name, null);
    }

    public void fetchUniversities(String name, String country) {
        Database database = DatabaseManager.getUniversityDatabase();

        Expression whereQueryExpression = Function.lower(Expression.property("name")).like(Expression.string("%" + name.toLowerCase() + "%"));

        if (country != null && !country.isEmpty()) {
            Expression countryQueryExpression = Function.lower(Expression.property("country")).like(Expression.string("%" + country.toLowerCase() + "%"));

            whereQueryExpression = whereQueryExpression.and(countryQueryExpression);
        }

        Query query = QueryBuilder.select(SelectResult.all())
                                  .from(DataSource.database(database))
                                  .where(whereQueryExpression);

        ResultSet rows = null;

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
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", row.getDictionary("universities").getString("name"));
            properties.put("country", row.getDictionary("universities").getString("country"));
            properties.put("web_pages", row.getDictionary("universities").getArray("web_pages"));
            // end::university[]

            data.add(properties);
        }

        mUniversitiesView.showUniversities(data);
    }
}
