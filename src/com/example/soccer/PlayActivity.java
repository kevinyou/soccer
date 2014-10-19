package com.example.soccer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class PlayActivity extends Activity implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

	private boolean running;
	
	private MapFragment mapFragment;
	
	private GoogleMap googleMap;
	
	private Location currentLocation;

	private LocationClient locationClient;
	
	private LocationRequest locationRequest;
	
	private static final double EARTH_RADIUS = 6371000;
	
	private double ballLat, ballLong,
				ballLatV, ballLongV,
				ballAccel;
	
	private Circle goalCircle;
	private Circle ballCircle;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(17);
		
		locationClient = new LocationClient(this, this, this);
		locationClient.connect();

		mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
		googleMap = mapFragment.getMap();
		googleMap.setMyLocationEnabled(true);
		googleMap.setIndoorEnabled(false);
		googleMap.setTrafficEnabled(false);
		
		googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
			public void onMapClick(LatLng point){
				//kick ball
				kick();
			}
		});
		
		UiSettings settings = googleMap.getUiSettings();
		settings.setCompassEnabled(false);
		settings.setTiltGesturesEnabled(false);
		settings.setZoomControlsEnabled(false);
		settings.setMyLocationButtonEnabled(false);
		
	}
	
	public void startGame(){
		double fps = 30.0;
		while (running){
			try{
				long start = System.currentTimeMillis();
				update();
				draw();
				long finish = System.currentTimeMillis();
				if (finish - start < 33){
					wait(33 - finish - start);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void update(){
		
	}
	
	public double dist(LatLng a, LatLng b){
		
		double alat = Math.toRadians(a.latitude);
		double alng = Math.toRadians(a.longitude);
		double blat = Math.toRadians(b.latitude);
		double blng = Math.toRadians(b.longitude);
		
		double c = Math.pow(Math.sin((alat - blat) / 2), 2) + Math.cos(alat) * Math.cos(blat) * Math.pow(Math.sin((alng - blng) / 2), 2);
		double d = 2 * Math.atan2(Math.sqrt(c), Math.sqrt(1 - c));
		return d * EARTH_RADIUS;
		
	}
	
	public void draw(){
		if (ballCircle != null) ballCircle.remove();
		CircleOptions ballCircleOptions = new CircleOptions()
			.center(new LatLng(ballLat, ballLong))
			.radius(10);
		ballCircle = googleMap.addCircle(ballCircleOptions);
	}

	public void kick(){
		LatLng playerLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		LatLng ballLatLng = new LatLng(ballLat, ballLong);
		Log.v("TESTINGU", "FALCON KICK");
		if (dist(playerLatLng, ballLatLng) <= 10){
			ballLong -= .0001;
			//replace with actual code later
			LatLng goalLatLng = goalCircle.getCenter();
			ballLatLng = new LatLng(ballLat, ballLong);
			if (dist(goalLatLng, ballLatLng) <= goalCircle.getRadius()){
				//you win! Yay!
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location newLoc) {
		currentLocation = newLoc;
		Log.v("TESTINGU", "UPDATED BITCH" + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
		draw();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		locationClient.requestLocationUpdates(locationRequest, this);
		currentLocation = locationClient.getLastLocation();
		Log.v("TESTINGU", "WTFBBZ" + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
		LatLng currentLocLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocLatLng, 20));
		
		//generate ball and goal
		CircleOptions goalCircleOptions = new CircleOptions()
			.center(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude() - .001))
			.radius(50);
		
		goalCircle = googleMap.addCircle(goalCircleOptions);
		
		ballLat = currentLocation.getLatitude();
		ballLong = currentLocation.getLongitude();
		
//		running = true;
//		
//		startGame();
		
	}

	@Override
	public void onDisconnected() {
		//connection lost
		running = false;
	}
}
