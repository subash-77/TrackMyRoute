package com.trackmyroute.mapdemo;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;
//----------------------------------|
//Gas_Station Fragment                |
//----------------------------------|
public class GasstationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private Marker mSelectedMarker;
    private Polyline mRouteLine;
    private GoogleMap mMap;
    private MapView mMapView;
    private View mView;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private List<Marker> mSchoolMarkers = new ArrayList<>();
    private Circle mEndMarker;
    FloatingActionButton recenterButton;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    SearchView searchView;

    FrameLayout map;
    Marker marker;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_education, container, false);
        //searchview
        map=mView.findViewById(R.id.map);
        searchView = mView.findViewById(R.id.search);
        searchView.clearFocus();
        //ads
        MobileAds.initialize(getActivity(), initializationStatus -> {
        });
        AdView adView = mView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        mMapView = mView.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        // Find the "Recenter" button by its ID
        recenterButton = mView.findViewById(R.id.recenter_button);

        // Set an OnClickListener on the button to recenter the map
        recenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the user's current location is available
                if (mLastLocation != null) {
                    // Create a CameraPosition object centered on the user's current location
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .zoom(15)
                            .tilt(45)
                            .build();

                    // Animate the camera to the new position
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
        //recenter end
        //searchview
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String loc= searchView.getQuery().toString();
                if(loc==null)
                {
                    Toast.makeText(getContext(), "Location Not Found", Toast.LENGTH_SHORT).show();
                }
                else {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(loc,1);
                        if(addressList.size()>0){
                            LatLng latLng = new LatLng(addressList.get(0).getLatitude(),addressList.get(0).getLongitude());
                            if(marker!=null){
                                marker.remove();
                            }
                            MarkerOptions markerOptions=new MarkerOptions().position(latLng).title(loc);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,10);
                            mMap.animateCamera(cameraUpdate);
                            marker = mMap.addMarker(markerOptions);
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return mView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Enable the My Location layer and show the default location button
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            // Move the default location button to the bottom right corner of the screen
            View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // Set the margin for the bottom and right edges
            //layoutParams.setMargins(0, 1600, 30, 30);
            googleMap.setPadding(0, 1600, 0,0);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        // Other code to set up the map...
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        mMap.setOnMarkerClickListener(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            //set our own image for location pointer
                            Drawable drawable = getResources().getDrawable(R.drawable.usercurrentlocation);
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 125, false);
                            // Create a custom marker for the user's location
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            // Add the custom marker to the map
                            mMap.addMarker(markerOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            searchNearbySchools();
                        }
                    }
                });
    }


    //---------------------------------------------------------------------------------------------------------------------------
    private void searchNearbySchools() {
        // Log.d("MapFragment", "Searching for nearby schools...");
        String schoolType = "gas_station";
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() +
                "&radius=7000" +
                "&type=" + schoolType +
                "&key=" + getString(R.string.google_maps_key);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d("MapFragment", "Google Places API response: " + response.toString());
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject school = results.getJSONObject(i);
                                JSONObject location = school.getJSONObject("geometry").getJSONObject("location");
                                double lat = location.getDouble("lat");
                                double lng = location.getDouble("lng");
                                String name = school.getString("name");
                                /*MarkerOptions markerOptions = new MarkerOptions()
                                        .position(new LatLng(lat, lng))
                                        .title(name);
                                Marker marker = mMap.addMarker(markerOptions);
                                mSchoolMarkers.add(marker);*/
                                Drawable drawable = getResources().getDrawable(R.drawable.location_image);
                                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(new LatLng(lat, lng))
                                        .title(name)
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                Marker marker = mMap.addMarker(markerOptions);
                                mSchoolMarkers.add(marker);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }
    //---------------------------------------------------------------------------------------------------------------------------
    //directions
    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Handle marker click
        mMap.clear();
        mSelectedMarker = marker;

        // Clear existing route line
        if (mRouteLine != null) {
            mRouteLine.remove();
        }

        // Get directions to selected marker
        if (mLastLocation != null && mSelectedMarker != null) {
            LatLng origin = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            LatLng dest = mSelectedMarker.getPosition();
            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + origin.latitude + "," + origin.longitude +
                    "&destination=" + dest.latitude + "," + dest.longitude +
                    "&key=" + getString(R.string.google_maps_key);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Parse route information from response
                                JSONArray routes = response.getJSONArray("routes");
                                JSONObject route = routes.getJSONObject(0);
                                JSONObject legs = route.getJSONArray("legs").getJSONObject(0);
                                JSONArray steps = legs.getJSONArray("steps");

                                // Extract polyline representing route
                                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                                String encodedPolyline = overviewPolyline.getString("points");
                                List<LatLng> decodedPolyline = PolyUtil.decode(encodedPolyline);

                                // Draw route on map
                                PolylineOptions routeOptions = new PolylineOptions()
                                        .addAll(decodedPolyline)
                                        .color(Color.parseColor("#004AAD"))
                                        .width(20);
                                mRouteLine = mMap.addPolyline(routeOptions);

                                // Calculate distance and duration between origin and destination
                                JSONObject distance1 = legs.getJSONObject("distance");
                                String distanceText = distance1.getString("text");
                                JSONObject duration = legs.getJSONObject("duration");
                                String durationText = duration.getString("text");

                                // Show distance and duration in an info window on the marker
                                String infoText = "Distance: " + distanceText + " Duration: " + durationText;

                                // Add turn-by-turn directions to info window
                                StringBuilder directionsText = new StringBuilder();
                                for (int i = 0; i < steps.length(); i++) {
                                    JSONObject step = steps.getJSONObject(i);
                                    String maneuver = step.optString("maneuver", "");
                                    String instruction = step.getString("html_instructions")
                                            .replaceAll("\\<.*?\\>", ""); // Remove HTML tags
                                    String distance = step.getJSONObject("distance").getString("text");

                                    // Add direction indication to instruction text
                                    if (!maneuver.isEmpty()) {
                                        String direction = "";
                                        if (maneuver.startsWith("turn-")) {
                                            String turn = maneuver.substring(5);
                                            direction = "Turn " + turn.replace("-", " ") + " ";
                                        } else if (maneuver.equals("uturn-left")) {
                                            direction = "Make a U-turn ";
                                        } else if (maneuver.equals("uturn-right")) {
                                            direction = "Make a U-turn ";
                                        }
                                        instruction = direction + instruction;
                                    }

                                    // Add step to directions text
                                    directionsText.append(i + 1)
                                            .append(". ")
                                            .append(instruction)
                                            .append(" (")
                                            .append(distance)
                                            .append(")\n");
                                }

                                // Show info window on marker
                                String ss=marker.getTitle();
                                mSelectedMarker.setTitle(marker.getTitle());
                                mSelectedMarker.setSnippet(infoText + " Direction: " + directionsText.toString());
                                mSelectedMarker.showInfoWindow();

                                // Add circle markers for start and end of the route
                                MarkerOptions markerOptions = new MarkerOptions().position(origin).title("My Current Location");
                                Drawable drawable = getResources().getDrawable(R.drawable.usercurrentlocation);
                                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                                markerOptions = new MarkerOptions()
                                        .position(origin)
                                        .title("My Current Location")
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                mMap.addMarker(markerOptions);
                                CircleOptions startMarkerOptions = new CircleOptions()
                                        .center(decodedPolyline.get(0))
                                        .radius(30)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.GREEN);
                                Circle startMarker = mMap.addCircle(startMarkerOptions);
                                //
                                if (mEndMarker != null) {
                                    mEndMarker.remove();
                                }

                                // Add circle marker for end of the route

                                MarkerOptions markerOptions1 = new MarkerOptions().position(dest);
                                Drawable drawable1 = getResources().getDrawable(R.drawable.location_image);
                                Bitmap bitmap1 = ((BitmapDrawable) drawable1).getBitmap();
                                Bitmap smallMarker1 = Bitmap.createScaledBitmap(bitmap1, 100, 100, false);
                                markerOptions1 = new MarkerOptions()
                                        .position(dest)
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker1));
                                mSelectedMarker = mMap.addMarker(markerOptions1);

                                // Set the marker's title and snippet
                                String infoText1 = "Distance: " + distanceText + " Duration: " + durationText;
                                StringBuilder directionsText2 = new StringBuilder();
                                // ... add code here to build the directionsText StringBuilder object ...
                                mSelectedMarker.setTitle(marker.getTitle());
                                mSelectedMarker.setSnippet(infoText1 + " Direction: " + directionsText2.toString());

                                // Show the info window
                                mSelectedMarker.showInfoWindow();

                                mMap.addMarker(markerOptions1);
                                CircleOptions endMarkerOptions = new CircleOptions()
                                        .center(decodedPolyline.get(decodedPolyline.size() - 1))
                                        .radius(30)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.RED);
                                mEndMarker = mMap.addCircle(endMarkerOptions);


                                // Tilt the map
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(decodedPolyline.get(0))
                                        .zoom(15)
                                        .tilt(45)
                                        .build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }



                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });

            Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
        }

        return false;
    }


}
