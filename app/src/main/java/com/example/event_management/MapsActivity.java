package com.example.event_management;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double lat, lng;
    private String title, price, locationName;
    private EditText edtSearch;
    private TextView tvPlaceName, tvPlaceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);
        title = getIntent().getStringExtra("title");
        price = getIntent().getStringExtra("price");
        locationName = getIntent().getStringExtra("location");

        edtSearch = findViewById(R.id.edtMapSearch);
        tvPlaceName = findViewById(R.id.tvMapPlaceName);
        tvPlaceInfo = findViewById(R.id.tvMapPlaceInfo);

        tvPlaceName.setText(title);
        if (price != null) tvPlaceInfo.setText("★ 5.0 • " + price);
        if (locationName != null) edtSearch.setText(locationName);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomIn());
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (mMap != null) {
                LatLng current = new LatLng(lat, lng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
            }
        });

        findViewById(R.id.btnGetDirections).setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(edtSearch.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                
                tvPlaceName.setText(locationName);
                tvPlaceInfo.setText(address.getAddressLine(0));
                
                // Update lat/lng for directions button
                this.lat = address.getLatitude();
                this.lng = address.getLongitude();
            } else {
                Toast.makeText(this, "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onBackClick(android.view.View view) {
        finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (lat != 0 && lng != 0) {
            LatLng location = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(location).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        } else if (locationName != null && !locationName.isEmpty()) {
            // Nếu không có tọa độ, tự động tìm kiếm theo tên địa chỉ ngay khi mở map
            searchLocation(locationName);
        }
    }
}
