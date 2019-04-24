package edu.temple.tuhub.models;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import com.androidnetworking.error.ANError;
import com.yelp.clientlib.entities.Business;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;

import edu.temple.tuhub.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import edu.temple.tuhub.R;

public class FoodTruck implements Serializable {

    public interface FoodTruckRequestListener {
        void onResponse(FoodTruck[] foodTrucks);

        void onError(ANError error);
    }

    private static final String DEFAULT_TERM = "Food";
    private static final String DEFAULT_LOCATION = "Temple University, Philadelphia, PA";
    private static final String SEARCH_LIMIT = "25";

    private static final String API_KEY = Yelp.API_KEY;

    private String name;
    private String rating;
    private String isClosed;
    private String longitude;
    private String latitude;
    private String imageURL;
    private String phone;

    private FoodTruck(String name, String rating, String isClosed, String longitude, String latitude, String imageURL, String phone) {
        this.name = name;
        this.rating = rating;
        this.isClosed = isClosed;
        this.longitude = longitude;
        this.latitude = latitude;
        this.imageURL = imageURL;
        this.phone = phone;
    }

    @Nullable
    public static FoodTruck createFoodTruck(String name, String rating, String isClosed, String longitude, String latitude,String imageURL, String phone) {
        if (name != null && rating != null && isClosed != null && longitude != null && latitude != null)
            return new FoodTruck(name, rating, isClosed, longitude, latitude, imageURL, phone);
        return null;
    }

    public String getName() {
        return name;
    }

    public String getRating() {
        return rating;
    }

    public String getIsClosed() {
        return isClosed;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getImageURL(){return imageURL;}

    public String getPhone(){return phone;}


    public static void retrieveFoodTrucks(final Double nwLat, final Double seLat, final Double nwLong, final Double seLong, final FoodTruckRequestListener foodTruckRequestListener) {


        // GET /businesses/search
        OkHttpClient client = new OkHttpClient();


        String term = DEFAULT_TERM;                       // term
        Double latitude = (seLat+nwLat)/2;
        Double longitude = (nwLong+seLong)/2;


        Request request = new Request.Builder()
                .url("https://api.yelp.com/v3/businesses/search?term=" + term + "&latitude=" + latitude + "&longitude=" + longitude + "")
                .get()
                .addHeader("authorization", "Bearer"+" "+API_KEY)
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();

            JSONObject jsonObject = new JSONObject(response.body().string().trim());       // parser
            JSONArray myResponse = (JSONArray)jsonObject.get("businesses");
            FoodTruck[] foodTrucks = new FoodTruck[myResponse.length()];
            for (int i=0; i < myResponse.length(); i++) {
                JSONObject o = myResponse.getJSONObject(i);
                foodTrucks[i] = new FoodTruck(
                        o.getString("name"),
                        o.getString("rating"),
                        o.getString("is_closed"),
                        o.getJSONObject("coordinates").getString("longitude"),
                        o.getJSONObject("coordinates").getString("latitude"),
                        o.getString("image_url"),
                        o.getString("phone"));
            }

            foodTruckRequestListener.onResponse(foodTrucks);

        } catch (Exception e) {
            System.out.println("REQUEST FAILED");
            e.printStackTrace();
        }

    }


        /*
        YelpAPIFactory apiFactory = new YelpAPIFactory(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        YelpAPI yelpAPI = apiFactory.createAPI();

        Map<String, String> params = new HashMap<>();

        // general params
        params.put("term", DEFAULT_TERM);
        params.put("limit", SEARCH_LIMIT);

        // locale params
        params.put("lang", "en");
        BoundingBoxOptions bo = new BoundingBoxOptions() {
            @Override
            public Double swLatitude() {
                return seLat;
            }

            @Override
            public Double swLongitude() {
                return nwLong;
            }

            @Override
            public Double neLatitude() {
                return nwLat;
            }

            @Override
            public Double neLongitude() {
                return seLong/;
            }
        };

        Call<SearchResponse> call = yelpAPI.search(bo, params);
        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {

                SearchResponse searchResponse = response.body();
                ArrayList<Business> businesses = searchResponse.businesses();
                FoodTruck[] foodTrucks = new FoodTruck[businesses.size()];
                for (int i = 0; i < businesses.size(); i++) {
                    FoodTruck ft = FoodTruck.createFoodTruck(
                            businesses.get(i).name(),
                            businesses.get(i).rating().toString(),
                            businesses.get(i).isClosed().toString(),
                            businesses.get(i).location().coordinate().longitude().toString(),
                            businesses.get(i).location().coordinate().latitude().toString(),
                            createFullSizeUrl(businesses.get(i).imageUrl()),
                            businesses.get(i).displayPhone()
                    );
                    foodTrucks[i] = ft;
                }
                foodTruckRequestListener.onResponse(foodTrucks);
                // Update UI text with the searchResponse.
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {

            }
        };

        call.enqueue(callback);
        */


    /*
    The Yelp API provides the URL of a thumbnail pic; this method replaces the
    size argument from "ms" (mega small I think) to "o" (original size)
     */
    public static String createFullSizeUrl(String url){
        if(url == null){
            return null;
        }
        int index = url.indexOf("ms.jpg");
        if(index == -1){
            return url;
        }
        url = url.substring(0, index);
        url += "o.jpg";
        return url;
    }

}
