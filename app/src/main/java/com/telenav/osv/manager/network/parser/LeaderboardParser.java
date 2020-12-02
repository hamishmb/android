package com.telenav.osv.manager.network.parser;

import org.json.JSONArray;
import org.json.JSONObject;
import com.telenav.osv.item.LeaderboardData;
import com.telenav.osv.item.network.UserCollection;

/**
 * JSON parser for leader board
 * Created by kalmanb on 8/1/17.
 */
public class LeaderboardParser extends ApiResponseParser<UserCollection> {

    private static final String TAG = "LeaderboardParser";

    @Override
    public UserCollection getHolder() {
        return new UserCollection();
    }

    public UserCollection parse(String json) {
        UserCollection collectionData = super.parse(json);
        if (json != null && !json.isEmpty()) {
            try {
                JSONObject obj = new JSONObject(json);
                JSONObject osv = obj.getJSONObject("osv");

                JSONArray users = osv.getJSONArray("users");
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    int rank = i + 1;
                    String userName = user.getString("username");
                    String fullName = user.getString("full_name");
                    String countryCode = user.getString("country_code");
                    int points = Integer.parseInt(user.getString("total_user_points"));
                    collectionData.getUserList().add(new LeaderboardData(userName, fullName, countryCode, rank, points));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return collectionData;
    }
}
