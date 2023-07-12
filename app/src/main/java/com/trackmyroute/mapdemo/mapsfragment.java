package com.trackmyroute.mapdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class mapsfragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    Location currentLocation;
    private Marker mSelectedMarker;
    FusedLocationProviderClient fusedClient;
    private static final int REQUEST_CODE = 101;
    FrameLayout map;
    GoogleMap gmap;
    Marker marker;
    private Location mLastLocation;
    SearchView searchView;
    FloatingActionButton recenterButton;
    private Circle mEndMarker;
    private Polyline mRouteLine;

    public mapsfragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mapsfragment, container, false);
        map = view.findViewById(R.id.map);
        searchView = view.findViewById(R.id.search);
        searchView.clearFocus();
        //ads
        MobileAds.initialize(getActivity(), initializationStatus -> {
        });
        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Find the "Recenter" button by its ID
        recenterButton = view.findViewById(R.id.recenter_button);

        //recenter the map to the user's current location on button click
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
                    gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

        fusedClient = LocationServices.getFusedLocationProviderClient(getContext());

        //create location request and update interval
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //create location callback
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                //get the last known location
                mLastLocation = locationResult.getLastLocation();

                //check if the map is initialized and move the camera to the user's current location
                if (gmap != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .zoom(20)
                            .tilt(45)
                            .build();
                    gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        };
        //recenter end
        fusedClient = LocationServices.getFusedLocationProviderClient(getContext());
        getLocation();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String loc = searchView.getQuery().toString();
                if (loc == null) {
                    Toast.makeText(getContext(), "Location Not Found", Toast.LENGTH_SHORT).show();
                } else {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(loc, 1);
                        if (addressList.size() > 0) {
                            LatLng latLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                            if (marker != null) {
                                marker.remove();
                            }
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(loc);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                            gmap.animateCamera(cameraUpdate);
                            marker = gmap.addMarker(markerOptions);
                        }
                    } catch (IOException e) {
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
        return view;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(mapsfragment.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gmap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        int padding = 0; // adjust this value as needed
        googleMap.setPadding(0, 1570, padding, padding);

        // Enable the My Location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latlng).title("My Current Location");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13));
        //set our own image for location pointer
        Drawable drawable = getResources().getDrawable(R.drawable.usercurrentlocation);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        markerOptions = new MarkerOptions()
                .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .title("My Current Location")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        googleMap.addMarker(markerOptions);
        gmap.setOnMarkerClickListener(this);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            //searchNearbySchools();
                        }
                    }
                });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocation();
            }
        }
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Handle marker click
        gmap.clear();
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
                                mRouteLine = gmap.addPolyline(routeOptions);

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
                                gmap.addMarker(markerOptions);
                                CircleOptions startMarkerOptions = new CircleOptions()
                                        .center(decodedPolyline.get(0))
                                        .radius(30)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.GREEN);
                                Circle startMarker = gmap.addCircle(startMarkerOptions);
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
                                mSelectedMarker = gmap.addMarker(markerOptions1);

                                // Set the marker's title and snippet
                                String infoText1 = "Distance: " + distanceText + " Duration: " + durationText;
                                StringBuilder directionsText2 = new StringBuilder();
                                // ... add code here to build the directionsText StringBuilder object ...
                                mSelectedMarker.setTitle(marker.getTitle());
                                mSelectedMarker.setSnippet(infoText1 + " Direction: " + directionsText2.toString());

                                // Show the info window
                                mSelectedMarker.showInfoWindow();

                                gmap.addMarker(markerOptions1);
                                CircleOptions endMarkerOptions = new CircleOptions()
                                        .center(decodedPolyline.get(decodedPolyline.size() - 1))
                                        .radius(30)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.RED);
                                mEndMarker = gmap.addCircle(endMarkerOptions);


                                // Tilt the map
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(decodedPolyline.get(0))
                                        .zoom(15)
                                        .tilt(45)
                                        .build();
                                gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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