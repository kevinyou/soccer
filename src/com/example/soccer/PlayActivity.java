package com.example.soccer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

	private boolean won, toastDisplayed;
	
	private Toast victoryToast;
	
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
		
		victoryToast = Toast.makeText(this, "You won! Tap the screen to make a new field.", 2000);
		
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
//				if (won){
//					startNewGame();
//				} else {
//					kick();
//				}
				kick();
			}
		});
		
		UiSettings settings = googleMap.getUiSettings();
		settings.setCompassEnabled(false);
		settings.setTiltGesturesEnabled(false);
		settings.setZoomControlsEnabled(false);
		settings.setMyLocationButtonEnabled(false);
		
	}
	
	public void startNewGame(){
		
		won = false;
		toastDisplayed = false;
		
		locationClient.requestLocationUpdates(locationRequest, this);
		currentLocation = locationClient.getLastLocation();
		Log.v("TESTINGU", "WTFBBZ" + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
		LatLng currentLocLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocLatLng, 20));
		
		//generate ball and goal
		CircleOptions goalCircleOptions = new CircleOptions()
			.center(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude() - .003))
			.radius(50);
		
		goalCircle = googleMap.addCircle(goalCircleOptions);
		
		ballLat = currentLocation.getLatitude();
		ballLong = currentLocation.getLongitude() - .0002;
	}
	
	public void draw(){
		if (ballCircle != null) ballCircle.remove();
		CircleOptions ballCircleOptions = new CircleOptions()
			.center(new LatLng(ballLat, ballLong))
			.fillColor(Color.BLACK)
			.radius(10);
		ballCircle = googleMap.addCircle(ballCircleOptions);
	}

	public void kick(){
		LatLng playerLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		LatLng ballLatLng = new LatLng(ballLat, ballLong);
		long start = System.currentTimeMillis();
		double playerBallDist = dist(playerLatLng, ballLatLng);
		if (playerBallDist <= 10){
			LatLng goalLatLng = goalCircle.getCenter();
			ballLatLng = ballMove(ballLatLng, bearing(playerLatLng, ballLatLng), playerBallDist);
			ballLat = ballLatLng.latitude;
			ballLong = ballLatLng.longitude;
			Log.v("TESTINGU", "FALCON KICK" + ballLatLng);
			if (dist(goalLatLng, ballLatLng) <= goalCircle.getRadius()){
				//won = true;
			}
		}
		long finish = System.currentTimeMillis();
		Log.v("TIMING", "kick() calculations took " + (finish - start) + " milliseconds");
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
	
	public double bearing(LatLng a, LatLng b){
		double alat = Math.toRadians(a.latitude);
		double alng = Math.toRadians(a.longitude);
		double blat = Math.toRadians(b.latitude);
		double blng = Math.toRadians(b.longitude);
		
		double y = Math.sin(blng - alng) * Math.cos(alat);
		double x = Math.cos(alat)  * Math.sin(blat) - Math.sin(alat) * Math.cos(blat) * Math.cos(blng - alng);
		
		double bearing = Math.atan2(y, x);
		
		Log.v("Bearing", "" + bearing);
		
		return bearing;
		
	}
	
	public LatLng ballMove(LatLng initial, double bearing, double distance){
		
		if (distance == 0) return initial;
		
		distance = 1 / distance;
		double lata = Math.toRadians(initial.latitude);
		double lnga = Math.toRadians(initial.longitude);
		
		double finalLat = Math.asin(Math.sin(lata) * Math.cos(distance / EARTH_RADIUS) + Math.cos(lata) * Math.sin(distance/ EARTH_RADIUS) * Math.cos(bearing));
		double finalLng = lnga + Math.atan2(Math.sin(bearing) * Math.sin(distance / EARTH_RADIUS) * Math.cos(lata), Math.cos(distance/ EARTH_RADIUS) - Math.sin(lata) * Math.sin(finalLat));
		
		return new LatLng(Math.toDegrees(finalLat), Math.toDegrees(finalLng));
		
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
		googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
		if (!toastDisplayed){
			toastDisplayed = true;
			victoryToast.show();
		}
		draw();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		startNewGame();
	}

	@Override
	public void onDisconnected() {
		//connection lost
	}
}
