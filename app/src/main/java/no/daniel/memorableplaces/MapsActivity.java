package no.daniel.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

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
    /**
     * Permission Request Codes.
     */
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 1;

    /**
     * Global Variable for GPS Location Requests.
     */
    private static final int LOCATION_REFRESH_DELAY = 10;
    private static final int LOCATION_REFRESH_DISTANCE = 10;

    private GoogleMap mMap;
    private Intent intent;
    private static LocationManager locationManager;
    private static LocationListener locationListener;

    /**
     * Move camera to the given location's latitude and longitude on the map.
     * @param location The location to move the camera to on the map.
     */
    private void moveCamToLocation(Location location) {
        if (location != null) {
            LatLng locLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 8.0f));
        }
    }

    /**
     * Called when the permission request is processed, meaning when the user decides
     * to grant or deny permission.
     * @param requestCode The request code defined by the request, see global request codes.
     * @param permissions The list of permissions requested.
     * @param grantResults The list of permissions grated or denied.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_DELAY, LOCATION_REFRESH_DISTANCE, locationListener);
            }
        }
    }

    /**
     * Called when the activity have been built.
     * @param savedInstanceState The last instance the activity was in.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        intent = getIntent();
    }


    /**
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

        if (intent != null) {
            // Populate Map from Intent.
            mMap.clear();
            List<Place> places = intent.getParcelableArrayListExtra("Places");
            if (places != null) {
                for (Place place : places) {
                    LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                }
            }

            // Move Camera to Place from Intent.
            Place place = intent.getParcelableExtra("Place");
            if (place != null) {
                LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8.0f));
            }
        }

        // Long Click Listener.
        mMap.setOnMapLongClickListener(location -> {
            // Reverse Geocoding.
            String street = "Somewhere";
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                Address address = addressList.get(0);
                if (address.getThoroughfare() != null) {
                    street = (address.getSubThoroughfare() != null) ? address.getSubThoroughfare() + " " + address.getThoroughfare() :
                            address.getThoroughfare();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            // Save Location.
            mMap.addMarker(new MarkerOptions().position(location).title(street));
            Toast.makeText(getApplicationContext(), R.string.toast_save_location, Toast.LENGTH_LONG).show();
            MainActivity.addPlace(new Place(street, location.latitude, location.longitude));
        });

        // Location Listener Declaration.
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                moveCamToLocation(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) { }
        };

        // Request Location Updates.
        if (Build.VERSION.SDK_INT < 23) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            moveCamToLocation(lastKnownLocation);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_DELAY, LOCATION_REFRESH_DISTANCE, locationListener);
            return;
        }

        // Check Location Permissions.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST_CODE);
            return;
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (intent.getParcelableExtra("Place") == null) {
            moveCamToLocation(lastKnownLocation);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_DELAY, LOCATION_REFRESH_DISTANCE, locationListener);
    }
}
