package com.example.eyesup.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.eyesup.R;
import com.example.eyesup.api.Constant;
import com.example.eyesup.api.NearbyPlacesJsonFormatter;
import com.example.eyesup.helper.GpsTracker;
import com.example.eyesup.model.Cafe;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MapFragment  extends  Fragment implements OnMapReadyCallback {

    public static final String TAG = "mapFragment";
    private static final float NORMAL_ZOOM = 15f;


    private GpsTracker gpsTracker;

    BroadcastReceiver bReceiver;

    private double mMyLatitude;
    private double mMyLongitude;

    double testLat = 21.543333;
    double testLon = 39.172779;

    ArrayList<Cafe> cafesAround;


    private GoogleMap mMap;

    Button btn;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        try {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        getLocation();

        bReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent != null){
                    mMyLatitude = intent.getDoubleExtra("latitude", mMyLatitude);
                    mMyLongitude = intent.getDoubleExtra("longitude", mMyLongitude);

                    LatLng myLocation = new LatLng(mMyLatitude, mMyLongitude);

                    mMap.addMarker(new MarkerOptions()
                            .position(myLocation)
                            .title("my location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, NORMAL_ZOOM));

                    getNearbyCafes(mMyLatitude, mMyLongitude, Constant.RADIUS_1KM);

                }

            }
        };




        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(true)
                .mapType(GoogleMap.MAP_TYPE_NORMAL);
        

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(bReceiver, new IntentFilter("location"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(bReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        String myLocationTitle = "my location";
        // Add a marker in Sydney and move the camera
        LatLng myLocation = new LatLng(mMyLatitude, mMyLongitude);
        mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title(myLocationTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, NORMAL_ZOOM));
        //because google directions api is NOT for free
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                          @Override
                                          public boolean onMarkerClick(Marker m) {
                                              if(m.getTitle().equals(myLocationTitle)){
                                                  if(m.isInfoWindowShown()){
                                                      m.hideInfoWindow();
                                                      return true;
                                                  }else {
                                                      m.showInfoWindow();
                                                      return true;
                                                  }
                                              }else {
                                                  Context ctx = requireContext();
                                                  LayoutInflater factory = LayoutInflater.from(ctx);
                                                  final View view = factory.inflate(R.layout.delete_confirmation_dialog, null);
                                                  final AlertDialog openMapsDialog = new AlertDialog.Builder(ctx).create();
                                                  openMapsDialog.setView(view);

                                                  TextView yes = view.findViewById(R.id.yes_btn);
                                                  TextView no = view.findViewById(R.id.no_btn);
                                                  TextView message = view.findViewById(R.id.message);

                                                  message.setText(ctx.getResources().getString(R.string.show_directions_on_map) + " " + m.getTitle());

                                                  yes.setOnClickListener(new View.OnClickListener() {
                                                      @Override
                                                      public void onClick(View v) {
                                                          Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                                  Uri.parse("http://maps.google.com/maps?daddr="+m.getPosition().latitude+","+m.getPosition().longitude));
                                                          startActivity(intent);
                                                          openMapsDialog.dismiss();
                                                      }
                                                  });

                                                  no.setOnClickListener(new View.OnClickListener() {
                                                      @Override
                                                      public void onClick(View v) {
                                                          openMapsDialog.dismiss();
                                                      }
                                                  });
                                                  openMapsDialog.show();
                                              }
                                              return true;
                                          }
                                      });

        getNearbyCafes(mMyLatitude, mMyLongitude, Constant.RADIUS_1KM);


    }

    public void getLocation(){
        gpsTracker = new GpsTracker(getActivity());
        if(gpsTracker.canGetLocation()){
            while (gpsTracker.getLatitude() == 0 || gpsTracker.getLongitude() == 0){
                gpsTracker.getLocation();
            }
            mMyLatitude = gpsTracker.getLatitude();
            mMyLongitude = gpsTracker.getLongitude();
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    public void getNearbyCafes(double lat, double lon, String radius){

        String url = Constant.MAPS_API_REQUEST_NEARBY_CAFES;
        String nearbyApiKey = getResources().getString(R.string.nearby_places_api_key);
        url += "key=" + nearbyApiKey + "&lat=" + lat + "&lon=" + lon + "&categorySet=" + Constant.CAFES_CATEGORY_SET + "&radius=" + radius;

        AndroidNetworking.get(url)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            NearbyPlacesJsonFormatter obj = new NearbyPlacesJsonFormatter(response);
                            cafesAround = obj.getCafes();

                            if(!cafesAround.isEmpty()){
                                for (Cafe cafe:cafesAround){
                                    LatLng cafeLocation = new LatLng(cafe.getLat(), cafe.getLon());
                                    mMap.addMarker(new MarkerOptions()
                                            .position(cafeLocation)
                                            .title(cafe.getName()))
                                            .setIcon(BitmapFromVector(getContext(), R.drawable.cafe));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}