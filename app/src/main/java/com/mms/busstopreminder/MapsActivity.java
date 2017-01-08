package com.mms.busstopreminder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.os.Vibrator;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

	private GoogleMap mMap;
	private EditText etLocInput;
	private EditText etRouteInput;
	private Spinner spinner;
	Context thisContext = this;
	String[] transitStopName;
	String[] transitStopID;
	private LocationManager mLocationManager;
	Marker currLocMarker = null;
	Marker targetLocMarker = null;
	LatLng targetLoc = null;

	int currItem = 1;

	Button prevButton;
	Button nextButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		addButtonListeners();
		replaceView(currItem);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
			return;
		}
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 1000, (float) 50.0, this);
	}

	void addButtonListeners() {
		prevButton = (Button) findViewById(R.id.btnPrev);
		prevButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				replaceView(--currItem);
			}
		});
		nextButton = (Button) findViewById(R.id.btnNext);
		nextButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				replaceView(++currItem);
			}
		});
	}

	void replaceView(int viewNum) {

		if(viewNum <= 1){
			prevButton.setEnabled(false);
			nextButton.setEnabled(true);
		}else if(viewNum >=2){
			prevButton.setEnabled(true);
			nextButton.setEnabled(false);
		}else{
			prevButton.setEnabled(true);
			nextButton.setEnabled(true);
		}

		View C = findViewById(R.id.topLinearLayout);
		ViewGroup parent = (ViewGroup) C.getParent();
		int index = parent.indexOfChild(C);
		parent.removeView(C);

		if (viewNum == 1) {
			C = getLayoutInflater().inflate(R.layout.option1_layout, parent, false);
			parent.addView(C, index);

			etLocInput = (EditText) findViewById(R.id.editTextLocInput);
			etLocInput.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// If the event is a key-down event on the "enter" button
					if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
							(keyCode == KeyEvent.KEYCODE_ENTER)) {
						// Perform action on key press
						// when user presses enter on etLocInput
						try {

							String input = etLocInput.getText().toString();
							ServerResponse response = new ServerCaller().execute("/stop_location?stop_code=" + input).get();

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
								targetLoc = destination;
								if(targetLocMarker == null) {
									targetLocMarker = mMap.addMarker(new MarkerOptions().position(destination).title("Your Destination"));
								}else{
									targetLocMarker.setPosition(targetLoc);
								}
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
			});
		} else if (viewNum == 2) {
			C = getLayoutInflater().inflate(R.layout.option2_layout, parent, false);
			parent.addView(C, index);

			etRouteInput = (EditText) findViewById(R.id.editTextRouteInput);

			spinner = (Spinner) findViewById(R.id.spinner);

			etRouteInput.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// If the event is a key-down event on the "enter" button
					if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
							(keyCode == KeyEvent.KEYCODE_ENTER)) {
						// Perform action on key press
						// when user presses enter on etLocInput
						try {

							String input = etRouteInput.getText().toString();
							ServerResponse response = new ServerCaller().execute("/stops?route=" + input).get();
			/*
			Log.i("resp code", response.getRespCode());
			Log.i("resp data", response.getData());
			Log.i("resp message", response.getMessage());*/
							Log.i("Transit Route", "Response code="+response.getRespCode());
							if (response.getRespCode().equals("0")) {
								//status is OK
								//{"message": "OK", "code": 0, "data": [["KENNEDY ARRIVE", "13398"], ["MCCOWAN ARRIVE", "13502"], ["MCCOWAN STATION - WESTBOUND PLATFORM", "14541"], ["SCARBOROUGH CENTRE STATION - WESTBOUND PLATFORM", "14542"], ["MIDLAND STATION - WESTBOUND PLATFORM", "14543"], ["ELLESMERE STATION - SOUTHBOUND PLATFORM", "14544"], ["LAWRENCE EAST STATION - SOUTHBOUND PLATFORM", "14545"], ["KENNEDY STATION - PLATFORM", "14546"], ["KENNEDY STATION - NORTHBOUND PLATFORM", "14547"], ["LAWRENCE EAST STATION - NORTHBOUND PLATFORM", "14548"], ["ELLESMERE STATION - NORTHBOUND PLATFORM", "14549"], ["MIDLAND STATION - EASTBOUND PLATFORM", "14550"], ["SCARBOROUGH CENTRE STATION - EASTBOUND PLATFORM", "14551"], ["MCCOWAN STATION - PLATFORM", "14552"]]}
								JSONArray arrOfPairs = response.getData();

								transitStopID = new String[arrOfPairs.length()];
								transitStopName = new String[arrOfPairs.length()];

								List<String> spinnerTransitStopNames = new ArrayList<String>();
								for(int i=0; i<arrOfPairs.length(); i++){
									transitStopName[i] = arrOfPairs.getJSONArray(i).getString(0);
									spinnerTransitStopNames.add(transitStopName[i]);
									Log.i("Spinner", "transit stop name="+transitStopName[i]);
									transitStopID[i] = arrOfPairs.getJSONArray(i).getString(1);
								}
								ArrayAdapter<String> adapter = new ArrayAdapter<String>(thisContext, android.R.layout.simple_spinner_item, spinnerTransitStopNames);
								adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

								spinner.setAdapter(adapter);
								spinner.setBackgroundColor(getResources().getColor(R.color.black));
								spinner.setSelection(0);

								spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
									@Override
									public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
										try {

											Log.i("transitStopID", transitStopID[position]);
											ServerResponse response = new ServerCaller().execute("/stop_location?stop_code=" + transitStopID[position]).get();

											Log.i("resp", response.getMessage());
											if(response.getRespCode().equals("0")){
												//status is OK
												//get the location
												double latitude = Double.parseDouble(response.getData().getString(0));
												double longitude = Double.parseDouble(response.getData().getString(1));

												LatLng destination = new LatLng(latitude, longitude);
												targetLoc = destination;
												if(targetLocMarker == null) {
													targetLocMarker = mMap.addMarker(new MarkerOptions().position(destination).title("Your Destination"));
												}else{
													targetLocMarker.setPosition(targetLoc);
												}
												mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));
											}
										} catch (InterruptedException e) {
											e.printStackTrace();
										} catch (ExecutionException e) {
											e.printStackTrace();
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}

									@Override
									public void onNothingSelected(AdapterView<?> parent) {

									}
								});
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
			});
		}
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
		/*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
			Log.i("ERROR", "no permission");
			return;
		}*/
		/*Location lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Log.i("Last known location", lastKnownLoc.toString());
		LatLng currLoc = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
		Log.i("currLoc", "Lat="+currLoc.latitude+"; Long="+currLoc.longitude);
		mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.currlocmarker)).title("You're here!").position(currLoc));
		//LatLng toronto = new LatLng(43.6532, -79.3832);
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 15));*/
		//LatLng toronto = new LatLng(43.6532, -79.3832);
		//mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("currlocmarker",50,50))).title("You're here!").position(toronto));
		//mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 17));
	}

	public Bitmap resizeMapIcons(String iconName, int width, int height) {
		Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
		return resizedBitmap;
	}

	@Override
	public void onLocationChanged(Location location) {
		//plot the new location
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();

		LatLng currLoc = new LatLng(latitude, longitude);
		Log.i("LocationService", "[location changed] Lat=" + latitude + "; Long=" + longitude);
		Log.i("Location", "currLoc=" + currLoc.toString() + "; targetLoc=" + targetLoc);
		Log.i("Location", "deltaLoc=" + deltaLoc(currLoc, targetLoc));

		float deltaLoc = deltaLoc(currLoc, targetLoc);
		if (deltaLoc <= 2000.0) {
			Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);
			Notification notification = new Notification.Builder(getApplicationContext())
					.setContentTitle("Wakey wakey :)")
					.setContentText("Your bus stop is almost here XD")
					.setContentIntent(pendingIntent)
					.addAction(android.R.drawable.sym_action_chat, "Aight!", pendingIntent)
					.setSmallIcon(android.R.drawable.sym_action_chat)
					.getNotification();
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			nManager.notify(1, notification);

			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

			v.vibrate(10000);
		}


		//mMap.addMarker(new MarkerOptions().position(currLoc).title("You're here!"));
		if (currLocMarker == null) {
			currLocMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("currlocmarker", 50, 50))).title("You're here!").position(currLoc));
		} else {
			//animateMarker(currLocMarker, currLoc, false);
			currLocMarker.setPosition(currLoc);
		}
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 15));
	}

	public float deltaLoc(LatLng currLoc, LatLng targetLoc) {
		Location curr = new Location("");
		curr.setLatitude(currLoc.latitude);
		curr.setLongitude(currLoc.longitude);

		Location target = new Location("");
		target.setLatitude(targetLoc.latitude);
		target.setLongitude(targetLoc.longitude);

		return curr.distanceTo(target);
	}

	public void animateMarker(final Marker marker, final LatLng toPosition,
							  final boolean hideMarker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = mMap.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 500;

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * toPosition.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * toPosition.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				} else {
					if (hideMarker) {
						marker.setVisible(false);
					} else {
						marker.setVisible(true);
					}
				}
			}
		});
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}
}