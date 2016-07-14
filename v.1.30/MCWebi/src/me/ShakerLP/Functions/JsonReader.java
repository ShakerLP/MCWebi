package me.ShakerLP.Functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/*
 * (Coptight) MCWebi by ShakerLP 
 * http://creativecommons.org/licenses/by-nd/4.0/
 */
public class JsonReader {
    private static String readAll(Reader rd) throws IOException {
        int cp;
        StringBuilder sb = new StringBuilder();
        while ((cp = rd.read()) != -1) {
            sb.append((char)cp);
        }
        return sb.toString();
    }

    public static JSONObject getJson(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            JSONObject json;
            JSONObject jSONObject;
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = JsonReader.readAll(rd);
            JSONObject jSONObject2 = jSONObject = (json = new JSONObject(jsonText));
            return jSONObject2;
        }
        finally {
            is.close();
        }
    }
    public static JSONArray getJsonArray(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            JSONArray json;
            JSONArray jSONObject;
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = JsonReader.readAll(rd);
            JSONArray jSONObject2 = jSONObject = (json = new JSONArray(jsonText));
            return jSONObject2;
        }
        finally {
            is.close();
        }
    }
}