package watnextstop.com.watnextstop;

import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.http.client.HttpClient;
import org.apache.http.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * Where location stuff takes place
 */

public class LocationStuff {
    public static JSONObject getDirections(double lat1, double lng1, double lat2, double lng2, String key) throws MalformedURLException, IOException{
        System.out.print(key);
        StringBuilder stringBuilder = new StringBuilder();


            /*HttpPost httppost = new HttpPost(url);

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();*/

        String urlstring = "https://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lng1 + "&destination=" + lat2 + "," + lng2 + "&mode=transit&sensor=false" + "&key=" + key;
        URL url = new URL(urlstring);
        URLConnection urlConnection = url.openConnection();
        StringBuilder result = new StringBuilder("");
        try {
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                result = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
        }
        catch (Exception e){System.out.println(e.getMessage());}
            /*response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {*/
        //}

        JSONObject jsonObject = new JSONObject();
        try {
            System.out.print(result.toString());
            jsonObject = new JSONObject(result.toString());
        } catch (JSONException e){System.out.print("Error");}
        return (jsonObject);
    }

    /*
    public static int getTransfers(JSONObject json) throws JSONException{
        JSONArray array = json.getJSONArray("routes");
        JSONObject routes = array.getJSONObject(0);
        JSONArray legs = routes.getJSONArray("legs");
        JSONObject legs0 = legs.getJSONObject(0); // the 0 object under legs
        JSONArray steps = legs0.getJSONArray("steps");
        return steps.length(); // size
    }
    */

    //updated version
    public static ArrayList<Transfer> getTransfers(JSONObject json) {
        ArrayList<Transfer> result = new ArrayList<Transfer>();
        System.out.println("getting transfers");
        try {
            JSONArray array = json.getJSONArray("routes");
            JSONObject routes = array.getJSONObject(0);
            JSONArray legs = routes.getJSONArray("legs");
            JSONObject legs0 = legs.getJSONObject(0); // the 0 object under legs
            JSONArray steps = legs0.getJSONArray("steps");
            for (int i = 0; i < steps.length(); i++) {
                JSONObject t = steps.getJSONObject(i);
                if (t.has("transit_details")) {
                    JSONObject details = t.getJSONObject("transit_details");
                    int numStops = details.getInt("num_stops");
                    JSONObject arrStop = details.getJSONObject("arrival_stop");
                    String name = arrStop.getString("name");
                    double lat = arrStop.getJSONObject("location").getDouble("lat");
                    double lng = arrStop.getJSONObject("location").getDouble("lng");
                    Transfer t_object = new Transfer(lat, lng, numStops, name);
                    result.add(t_object);
                }
            }
            System.out.println(result.size() + " transfers");
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            return result;
        }
    }
}





