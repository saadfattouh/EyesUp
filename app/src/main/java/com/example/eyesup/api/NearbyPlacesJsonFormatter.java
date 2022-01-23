package com.example.eyesup.api;

import com.example.eyesup.model.Cafe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NearbyPlacesJsonFormatter {

    JSONObject jsonResponse;
    JSONObject summary;
    JSONArray results;
    ArrayList<Cafe> cafes;

    public NearbyPlacesJsonFormatter(JSONObject response) throws JSONException {
        this.jsonResponse = response;
        cafes = new ArrayList<>();
        summary = jsonResponse.getJSONObject("summary");
        results = jsonResponse.getJSONArray("results");
    }

    public ArrayList<Cafe> getCafes(){

        try {
            int numResults = getNUmResults();
            if(numResults > 0){
                for(int i = 0; i < results.length(); i++){
                    JSONObject result = (JSONObject) results.get(i);
                    double dist = result.getDouble("dist");
                    JSONObject poi = result.getJSONObject("poi");
                    String name = poi.getString("name");
                    String phone = "";
                    if(poi.has("phone"))
                        phone = poi.getString("phone");
                    JSONObject position = result.getJSONObject("position");
                    Double lat = position.getDouble("lat");
                    Double lon = position.getDouble("lon");

                    Cafe cafe = new Cafe(dist, name, phone, lat, lon);

                    cafes.add(cafe);
                }
            }else{
                return cafes;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this.cafes;
    }

    public int getNUmResults() throws JSONException {
        int numResults = this.summary.getInt("numResults");
        return numResults;
    }


}
