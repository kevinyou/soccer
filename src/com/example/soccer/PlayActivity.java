package com.example.soccer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
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

	private boolean started, won, toastDisplayed;
	
	private Toast victoryToast, loading;
	
	private MapFragment mapFragment;
	
	private GoogleMap googleMap;
	
	private Location currentLocation;

	private LocationClient locationClient; 
	
	private LocationRequest locationRequest;
	
	private static final Calendar c = Calendar.getInstance();
	
	private static final double EARTH_RADIUS = 6371000;
	
	private double ballLat, ballLong;
	
	private int kicks;
	
	private Circle goalCircle;
	private Circle ballCircle;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		
		victoryToast = Toast.makeText(this, "You won! Tap the screen to make a new field.", 2000);
		loading = Toast.makeText(this, "Loading...", Toast.LENGTH_LONG);
		
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
				if (started){
					if (won){
						startNewGame();
					} else {
						kick();
					}
				}
			}
		});
		
		UiSettings settings = googleMap.getUiSettings();
		settings.setCompassEnabled(false);
		settings.setTiltGesturesEnabled(false);
		settings.setZoomControlsEnabled(false);
		settings.setMyLocationButtonEnabled(false);
		
		loading.show();
		
	}
	
	public void startNewGame(){
		
		kicks = 0;
		
		locationClient.requestLocationUpdates(locationRequest, this);
		currentLocation = locationClient.getLastLocation();
		LatLng currentLocLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocLatLng, 20));
		
		//generate ball and goal
		CircleOptions goalCircleOptions = new CircleOptions()
			.center(new LatLng(currentLocation.getLatitude() + (.004 * Math.random() - .002), currentLocation.getLongitude() + (.004 * Math.random() - .002)))
			.radius(50);
		
		goalCircle = googleMap.addCircle(goalCircleOptions);
		
		ballLat = currentLocation.getLatitude() + (.001 * Math.random() - .0005);
		ballLong = currentLocation.getLongitude() + (.001 * Math.random() - .0005);
		
		won = false;
		toastDisplayed = false;
		started = true;
	}
	
	public void draw(){
		if (ballCircle != null) ballCircle.remove();
		CircleOptions ballCircleOptions = new CircleOptions()
			.center(new LatLng(ballLat, ballLong))
			.fillColor(Color.BLACK)
			.radius(15);
		ballCircle = googleMap.addCircle(ballCircleOptions);
	}

	public void kick(){
		Log.v("TESTINGU", "FALCON KICK " + kicks);
		LatLng playerLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		LatLng ballLatLng = new LatLng(ballLat, ballLong);
		double playerBallDist = dist(playerLatLng, ballLatLng);
		if (playerBallDist <= 20){
			kicks++;
			LatLng goalLatLng = goalCircle.getCenter();
			ballLatLng = ballMove(ballLatLng, bearing(playerLatLng, ballLatLng), playerBallDist);
			//ballLatLng = goalCircle.getCenter();
			ballLat = ballLatLng.latitude;
			ballLong = ballLatLng.longitude;
			if (dist(goalLatLng, ballLatLng) <= goalCircle.getRadius()){
				won = true;
			}
		}
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
		
		if (distance == 0) distance = 50;
		else distance = 40 / distance + 18;
		
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

	@SuppressLint("NewApi")
	@Override
	public void onLocationChanged(Location newLoc) {
		currentLocation = newLoc;
		Log.v("TESTINGU", "UPDATED " + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
		googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
		if (started && won && !toastDisplayed){
			toastDisplayed = true;
			victoryToast.show();
			try {
				SharedPreferences sp = getSharedPreferences("com.example.soccer.DATA", MODE_PRIVATE);
				SharedPreferences.Editor spEditor = sp.edit();
				Date d = c.getTime();	
				spEditor.putInt(d.toString(), kicks);
				spEditor.apply();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
		loading.cancel();
		Log.v("CONNECTION", "WE GOOD YO!");
		startNewGame();
	}

	@Override
	public void onDisconnected() {
		//connection lost
	}
}
