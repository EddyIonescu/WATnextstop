package watnextstop.com.watnextstop;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import java.io.IOException;
import java.net.HttpURLConnection;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import watnextstop.com.watnextstop.LocationStuff;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private GoogleMap mMap;
    private Marker destination;
    private LatLng currentLocation = new LatLng(43.4732258, -80.5436222); //defaults to M3
    //whether the end value have been initialized
    private boolean destination_init = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /**
     * - part of MapsActivity Template
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            mMap.setMyLocationEnabled(true);
            mMap.setBuildingsEnabled(true);
            mMap.setTrafficEnabled(true);
        }
        catch (SecurityException se){
            System.out.println("Permission problems for getting location");
        }
        mMap.setOnMyLocationChangeListener(myLocationChangeListener); //deprecated: see below
        mMap.setOnMapClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }

    //it's deprecated but is much easier to use todo: use google play services instead
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            if(mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));

                //check if we're close to destination
                float distance = 0;
                if(destination_init) {
                    distance = getDistance(destination.getPosition(), currentLocation);
                    System.out.println("distance: " + distance);
                    if (distance <= distAlert1) {
                        sendDestinationAlert();
                        System.out.println("send dest alert");
                    } else if (distance <= distAlert2) {
                        sendApproachAlert();
                        System.out.println("send approach alert");
                    }
                }
            }
        }
    };
    public void onMapClick(LatLng point){
            if(destination_init) destination.remove();
            destination = mMap.addMarker(new MarkerOptions().position(point).title("Destination").draggable(true).visible(true));
            destination.showInfoWindow();
            destination_init = true;

            //get the directions - open new activity, then go back
        System.out.println("Getting directions");
        System.out.println("# of transfers: ");
        ArrayList<Transfer> transfers = doDirections();
        System.out.println(transfers.size() + " transfers");
        String messagetoshow = "";
        messagetoshow += ((transfers.size() == 1) ? "There is " : "There are ") + transfers.size() + ((transfers.size() == 1) ? " transfer \n" : " transfers \n");
        for(Transfer t : transfers){
            messagetoshow += t.stopNum + " stops; your stop is: " + t.lastLoc + " on the " + t.busHeader + " bus" + "\n";
        }
        new AlertDialog.Builder(this)
                .setTitle("Your Route")
                .setMessage(messagetoshow)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    public ArrayList<Transfer> doDirections(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //this is the server key - not the android api key
        String key = "AIzaSyCJ_13eGsrcohQfZSdkCh0e92Cm7-c84Y8"; //todo: consider not hardcoding api keys in open-source code
        try {
            JSONObject json = new JSONObject();
            json = LocationStuff.getDirections(currentLocation.latitude, currentLocation.longitude,
                    destination.getPosition().latitude, destination.getPosition().longitude,key);
            System.out.println("got json");

            return LocationStuff.getTransfers(json);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new ArrayList<Transfer>();
        }
    }

    // time to destination
    public float getDistance(LatLng dest, LatLng cur) {

        //no
        /*
        Location destloc = new Location(LocationManager.GPS_PROVIDER);
        Location curloc = new Location(LocationManager.GPS_PROVIDER);

        System.out.println(cur.latitude + ", " + cur.longitude);
        System.out.println(dest.latitude + ", " + dest.longitude);

        destloc.setLatitude(dest.latitude);
        destloc.setLongitude(dest.longitude);

        curloc.setLatitude(cur.latitude);
        curloc.setLatitude(cur.longitude);

        float distance = curloc.distanceTo(destloc);
        System.out.println("distance1:" + distance);
        */

        return (float)distance(cur.latitude, cur.longitude, destination.getPosition().latitude, destination.getPosition().longitude);
    }

    //http://stackoverflow.com/questions/6981916/how-to-calculate-distance-between-two-locations-using-their-longitude-and-latitu
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist*1000); //km to m
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public static float distAlert1 = 100f;
    public static float distAlert2 = 200f;

    public static final int ALERT_ID1 = 1;
    public static final int ALERT_ID2 = 2;

    public void sendDestinationAlert() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.cast_ic_notification_0);

        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setContentTitle("Approaching destination!");
        builder.setContentText("You are reaching your destination!");
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(ALERT_ID1, builder.build());
    }

    public void sendApproachAlert() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.cast_ic_notification_0);

        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setContentTitle("Reaching soon...");
        builder.setContentText("Almost there...");
        builder.setVibrate(new long[]{2000, 2000, 2000, 2000, 2000});

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(ALERT_ID2, builder.build());
    }

}
