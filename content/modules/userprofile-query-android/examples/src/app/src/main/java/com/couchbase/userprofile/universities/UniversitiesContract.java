package com.couchbase.userprofile.universities;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class UniversitiesContract {
    interface View {
        void showUniversities(List<Map<String, Object>> universities);
    }
    interface UserActionsListener {
        void fetchUniversities(String name, String country);
        void fetchUniversities(String name);
    }
}
