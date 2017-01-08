package com.mms.busstopreminder;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnKeyListener {

	private GoogleMap mMap;

	private EditText etLocInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		etLocInput = (EditText) findViewById(R.id.editTextLocInput);
		etLocInput.setOnKeyListener(this);

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

		// Add a marker in Sydney and move the camera
		LatLng toronto = new LatLng(43.6532, -79.3832);
		mMap.addMarker(new MarkerOptions().position(toronto).title("Marker in toronto"));
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 10));
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// If the event is a key-down event on the "enter" button
		if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
				(keyCode == KeyEvent.KEYCODE_ENTER)) {
			// Perform action on key press
			// when user presses enter on etLocInput
			try {

				String input = etLocInput.getText().toString();
				ServerResponse response = new ServerCaller().execute(input).get();

			/*
			Log.i("resp code", response.getRespCode());
			Log.i("resp data", response.getData());
			Log.i("resp message", response.getMessage());*/

				if (response.getRespCode().equals("0")) {
					//status is OK
					//get the location
					double latitude = Double.parseDouble(response.getData().getString(0));
					double longitude = Double.parseDouble(response.getData().getString(1));

					LatLng destination = new LatLng(latitude, longitude);
					mMap.addMarker(new MarkerOptions().position(destination).title("Your Destination"));
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));
				} else {
					//server error
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
}
