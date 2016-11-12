package org.atthack.november16.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jwalters on 11/12/2016.
 */

public class POI {

    String name;
    double lat;
    double lng;
    String description;
    String imageURL;
    String audio;

    public POI () {}

    public POI (JSONObject json) {
        fromJSON(json);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public void fromJSON(JSONObject json) {
        try {

            name = json.getString("name");
            lat = json.getDouble("lat");
            lng = json.getDouble("lng");
            description = json.getString("description");
            imageURL = json.getString("imageURL");
            audio = json.getString("audio");

        } catch (JSONException e) {

        }
    }
}
