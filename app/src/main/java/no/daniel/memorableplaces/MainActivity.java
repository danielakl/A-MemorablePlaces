package no.daniel.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.daniel.memorableplaces.ObjectSerializer.*;

/**
 * The main activity of this app.
 */
public class MainActivity extends AppCompatActivity {
    private static final ArrayList<Place> placesList = new ArrayList<>();
    private static ArrayAdapter arrayAdapter;
    private static SharedPreferences sharedPreferences;

    /**
     * Starts up the map activity and sends all the place information.
     * @param view the (button) view that called this method.
     */
    public void goToMap(View view) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("Places", placesList);
        startActivity(intent);
    }

    /**
     * Add a place in the ListView. Currently used by other activities.
     * @param place the place to add to the list.
     */
    public static void addPlace(Place place) {
        if (place != null) {
            placesList.add(place);
            if (arrayAdapter != null) {
                arrayAdapter.notifyDataSetChanged();
            }
            if (sharedPreferences != null) {
                try {
                    sharedPreferences.edit().putString("Places", serialize(placesList)).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Add a collection of places to the ListView.
     * @param places the places to add.
     */
    public static void addPlace(List<Place> places) {
        if (places != null) {
            placesList.addAll(places);
            if (arrayAdapter != null) {
                arrayAdapter.notifyDataSetChanged();
            }
            try {
                if (sharedPreferences != null) {
                    sharedPreferences.edit().putString("Places", serialize(placesList)).apply();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView placesListView = findViewById(R.id.placesList);

        if (arrayAdapter == null || placesListView.getAdapter() == null) {
            arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, placesList);
            placesListView.setAdapter(arrayAdapter);
            placesListView.setOnItemClickListener((adapterView, view, position, l) -> {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("Places", placesList);
                intent.putExtra("Place", (Place) adapterView.getItemAtPosition(position));
                startActivity(intent);
            });
//        placesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
//                return false;
//            }
//        });
        }

        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        try {
            ArrayList<Place> places = (ArrayList<Place>) deserialize(sharedPreferences.getString("Places", serialize(new ArrayList<Place>())));
            placesList.clear();
            addPlace(places);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
