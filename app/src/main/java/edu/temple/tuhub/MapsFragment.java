package edu.temple.tuhub;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import edu.temple.tuhub.models.Building;
import edu.temple.tuhub.models.FoodTruck;
import edu.temple.tuhub.models.User;

public class MapsFragment extends Fragment {

    private GoogleMap googleMap;
    private MapView mMapView;
    private String currentCampus;
    private Building[] Buildings;
    private FoodTruck[] FoodTrucks;
    private Button detailBtn;
    private Marker currentMarker;
    public static Boolean ignoreSharedPreferences = false;
    boolean AmblerNoOption = true;

    public MapsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        detailBtn = (Button) v.findViewById(R.id.mapDetailsButton);
        detailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDetails();
            }
        });
        if (!ignoreSharedPreferences) {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("MapsPreferences", Context.MODE_PRIVATE);
            if (User.CURRENT != null) {
                currentCampus = sharedPref.getString(User.CURRENT.getTuID() + "MapPreference", getString(R.string.saved_default_map));
            } else {
                currentCampus = sharedPref.getString("GuestMapPreference", getString(R.string.saved_default_map));
            }
        }
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                // For showing a move to my location button
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                1);
                    }
                } else {
                    // For zooming automatically to the location of the marker
                    Building.retrieveBuildings(currentCampus, new Building.BuildingRequestListener() {
                        @Override
                        public void onResponse(Building[] buildingResponse) {
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(Building.getcampusLatLng()).zoom(16).build();
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            Buildings = new Building[buildingResponse.length];
                            for (int i = 0; i < buildingResponse.length; i++) {
                                Buildings[i] = buildingResponse[i];
                                if (buildingResponse[i] != null) {
                                    if (buildingResponse[i].getName().toLowerCase().contains("library")) {
                                        googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(buildingResponse[i].getLatitude()), Double.parseDouble(buildingResponse[i].getLongitude()))).title(buildingResponse[i].getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.book)));
                                    } else if (buildingResponse[i].getName().toLowerCase().contains("tech center")) {
                                        googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(buildingResponse[i].getLatitude()), Double.parseDouble(buildingResponse[i].getLongitude()))).title(buildingResponse[i].getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.computers)));
                                    } else
                                        googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(buildingResponse[i].getLatitude()), Double.parseDouble(buildingResponse[i].getLongitude()))).title(buildingResponse[i].getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.university)));
                                }
                            }
                            FoodTruck.retrieveFoodTrucks(Building.getNorthWestLatitude(), Building.getSouthEastLatitude(), Building.getNorthWestLongitude(), Building.getSouthEastLongitude(), new FoodTruck.FoodTruckRequestListener() {
                                @Override
                                public void onResponse(FoodTruck[] foodTrucks) {
                                    FoodTrucks = new FoodTruck[foodTrucks.length];
                                    for (int i = 0; i < foodTrucks.length; i++) {
                                        FoodTrucks[i] = foodTrucks[i];
                                        if (foodTrucks[i] != null) {
                                            googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(foodTrucks[i].getLatitude()), Double.parseDouble(foodTrucks[i].getLongitude()))).title(foodTrucks[i].getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant)));
                                        }
                                    }
                                }

                                @Override
                                public void onError(ANError error) {
                                }
                            });
                        }

                        @Override
                        public void onError(ANError error) {
                        }
                    });
                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            marker.showInfoWindow();
                            currentMarker = marker;
                            if (detailBtn != null)
                                detailBtn.setVisibility(View.VISIBLE);
                            return false;
                        }
                    });
                    googleMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
                        @Override
                        public void onInfoWindowClose(Marker marker) {
                            currentMarker = null;
                            if (detailBtn != null)
                                detailBtn.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
        return v;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
    }

    private void loadDetails() {
        if (currentMarker != null) {
            for (edu.temple.tuhub.models.Building Building : Buildings) {
                if (Building.getName().equals(currentMarker.getTitle())) {
                    activity.loadBuildingDetails(Building.getName(), Building.getImageUrl(), Building.getLatitude(), Building.getLongitude());
                }
            }
            for (edu.temple.tuhub.models.FoodTruck FoodTruck : FoodTrucks) {
                if (FoodTruck.getName().equals(currentMarker.getTitle())) {
                    activity2.loadFoodTruckDetails(FoodTruck.getName(), FoodTruck.getRating(), FoodTruck.getIsClosed(), FoodTruck.getLatitude(), FoodTruck.getLongitude(), FoodTruck.getImageURL(), FoodTruck.getPhone());
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu_map, menu);
        MenuItem item = menu.findItem(R.id.searchMaps);
        SearchView sv = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, sv);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Building> buildingResults = new ArrayList();
                for (int i = 0; i < Buildings.length; i++) {
                    if (Buildings[i].getName().toLowerCase().contains(query.toLowerCase()))
                        buildingResults.add(Buildings[i]);
                }
                ArrayList<FoodTruck> foodTruckResults = new ArrayList();
                for (int i = 0; i < FoodTrucks.length; i++) {
                    if (FoodTrucks[i].getName().toLowerCase().contains(query.toLowerCase()))
                        foodTruckResults.add(FoodTrucks[i]);
                }
                Building[] bldRslt = new Building[buildingResults.size()];
                FoodTruck[] fdTrkRslt = new FoodTruck[foodTruckResults.size()];
                for (int i = 0; i < buildingResults.size(); i++) {
                    bldRslt[i] = buildingResults.get(i);
                }
                for (int i = 0; i < foodTruckResults.size(); i++) {
                    fdTrkRslt[i] = foodTruckResults.get(i);
                }
                if (fdTrkRslt.length == 0 && bldRslt.length == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "No results", Toast.LENGTH_SHORT).show();
                } else {
                    activity4.mapSearchResults(bldRslt, fdTrkRslt);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.searchMaps:
                return true;
            case R.id.menu_change_campus:
                //final String tuid = preferences.getString(getString(R.string.user_id_key), "");
                final String[] Campuses = {"Ambler", "Center City", "Health Sciences", "Japan", "Main"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Change Campus")
                        .setSingleChoiceItems(Campuses, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AmblerNoOption = false;
                                currentCampus = Campuses[i];
                            }
                        });
                builder.setPositiveButton(R.string.SetDefault, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = getActivity().getSharedPreferences("MapsPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        if(AmblerNoOption)
                            currentCampus = "Ambler";
                        if (User.CURRENT != null) {
                            editor.putString(User.CURRENT.getTuID() + "MapPreference", currentCampus);
                        } else {
                            editor.putString("GuestMapPreference", currentCampus);
                        }
                        editor.apply();
                        activity3.reloadMap();
                    }
                });
                builder.setNeutralButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(AmblerNoOption)
                            currentCampus = "Ambler";
                        ignoreSharedPreferences = true;
                        activity3.reloadMap();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                AmblerNoOption = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null)
            mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null)
            mMapView.onLowMemory();
    }

    loadBuildingDetailsInterface activity;
    loadFoodTruckDetailsInterface activity2;
    reloadMapInterface activity3;
    mapSearch activity4;

    @Override
    public void onAttach(Activity c) {
        super.onAttach(c);
        activity = (loadBuildingDetailsInterface) c;
        activity2 = (loadFoodTruckDetailsInterface) c;
        activity3 = (reloadMapInterface) c;
        activity4 = (mapSearch) c;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        activity2 = null;
        activity3 = null;
        activity4 = null;
    }

    interface loadBuildingDetailsInterface {
        void loadBuildingDetails(String name, String imageUrl, String latitude, String longitude);
    }

    interface loadFoodTruckDetailsInterface {
        void loadFoodTruckDetails(String name, String rating, String isClosed, String latitude, String longitude, String imageURL, String phone);
    }

    interface reloadMapInterface {
        void reloadMap();
    }

    interface mapSearch {
        void mapSearchResults(Building[] buildings, FoodTruck[] foodTrucks);
    }
}


