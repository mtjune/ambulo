package com.example.ambulo1;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class amblo1_service extends Service implements SensorEventListener, LocationListener{
	
	private static  String SERVER_URL2 = "http://192.168.43.91";
	private static final String URL_REGULAR = "/regular/regular.php";
	private static final String URL_RANDOM = "/random/random.php";
	
	private SensorManager sensorManager;
	private Sensor sensor;
	private LocationManager locationManager;
	
	private double position_X,position_Y;
	private float x=0,y=0,z=0;
	private float acceleration,old_acceleration,acceleration_max,acceleration_min;
	private long new_time,old_time,fall_time1,fall_time2,fall_time3,fall_jadge1,fall_jadge2,fall_jadge3;
	private int hosu;
	private boolean up_down; /* true = up */
	private boolean  location_start;
	private boolean fall_flag1,fall_flag2,fall_flag3;
	private String Decided_URL,lost_flag;
	
	private BTtoSerial BS2 = MainActivity.BS;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate(){
		android.util.Log.v("service", "onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		android.util.Log.v("service", "onStartCommand");	
		
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this,  sensor, SensorManager.SENSOR_DELAY_UI);
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, this);
		
		SERVER_URL2 = MainActivity.SERVER_URL;
		
		acceleration_max= (float)9.8;
		acceleration_min= (float)9.8;
		up_down = false;
		location_start = false;
		fall_flag1 = false;
		fall_flag2 = false;
		fall_flag3 = false;
		old_time = new Date().getTime();
		hosu = 0;
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		android.util.Log.v("service", "onDestroy");
		BS2.BDestroy();
		sensorManager.unregisterListener(this);
		locationManager.removeUpdates(this);
	}
	
	private void tumble_connect() {
		SharedPreferences pref = getSharedPreferences("AMBULO1_pref", MODE_PRIVATE);
		
		StringBuffer strbuff = new StringBuffer(SERVER_URL2);
		strbuff.append(URL_RANDOM)
			.append("?sNumber=")
			.append(pref.getString("sNumber", "Not_set"))
			.append("&posix=")
			.append(Double.toString(position_X))
			.append("&posiy=")
			.append(Double.toString(position_Y))
			.append("&Accident=")
			.append("2")
			.append("&meetID=")
			.append("0");
		
		Decided_URL = strbuff.toString();
		
		HttpURLConnection httpCon3 = null;
		InputStream in3 = null;

		try {
			URL url3 = new URL(Decided_URL);
			httpCon3 = (HttpURLConnection) url3.openConnection();
			httpCon3.setRequestMethod("GET");
			httpCon3.connect();
			
			android.util.Log.v("tumble", "Http connect");

			in3 = httpCon3.getInputStream();

			String back_SERVER = new String();
			byte[] line = new byte[1024];
			int size;

			while (true) {
				size = in3.read(line);
				if (size <= 0)
					break;
				back_SERVER += new String(line);		}
		} catch (Exception e) {
			e.printStackTrace();
			android.util.Log.v("tumble", "Http error1");
		} finally {
			try {
				if (httpCon3 != null)
					httpCon3.disconnect();
				if (in3 != null)
					in3.close();
			} catch (Exception e) {
				e.printStackTrace();
				android.util.Log.v("tumble", "Http error2");
			}
		}
	}

	private void location_connect(){
		SharedPreferences pref = getSharedPreferences("AMBULO1_pref", MODE_PRIVATE);
		
		StringBuffer strBuff = new StringBuffer(	SERVER_URL2);
		strBuff.append(URL_REGULAR)
			.append("?sNumber=")
			.append(pref.getString("sNumber", "Not_set"))
			.append("&hosu=")
			.append(Integer.toString(hosu))
			.append("&posix=")
			.append(Double.toString(position_X))
			.append("&posiy=")
			.append(Double.toString(position_Y))
			.append("&free=")
			.append("0");

		Decided_URL = strBuff.toString();

		HttpURLConnection httpCon2 = null;
		InputStream in2 = null;

		try {
			URL url2 = new URL(Decided_URL);
			httpCon2 = (HttpURLConnection) url2.openConnection();
			httpCon2.setRequestMethod("GET");
			httpCon2.connect();
			
			android.util.Log.v("location", "Http connect");

			in2 = httpCon2.getInputStream();

			lost_flag = new String();
			byte[] line = new byte[1024];
			int size;

			while (true) {
				size = in2.read(line);
				if (size <= 0)
					break;
				lost_flag += new String(line);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			android.util.Log.v("location", "Http error1");
		} finally {
			try {
				if (httpCon2 != null)
					httpCon2.disconnect();
				if (in2 != null)
					in2.close();
			} catch (Exception e) {
				e.printStackTrace();
				android.util.Log.v("location", "Http error2");
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		position_X = location.getLatitude();
		position_Y = location.getLongitude();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(location_start == true){
					location_connect();
					hosu = 0;
					char c = lost_flag.charAt(0);
					if((c == '1') || (c == '2'))
						BS2.LightLED(4);
					else
						BS2.LightLED(0);
				}else
					location_start = true;
				
				Calendar calendar1;
				calendar1 = Calendar.getInstance();
				int hour = calendar1.get(Calendar.HOUR_OF_DAY);
				if((hour < 5) || (hour >= 16))
				BS2.LightLED(2);
			}
		}).start();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			
			x = (float)(x*0.9 + event.values[0]*0.1);
			y = (float)(y*0.9 + event.values[1]*0.1);
			z = (float)(z*0.9 + event.values[2]*0.1);
			
			old_acceleration = acceleration;
			acceleration = (float)Math.sqrt(x*x+y*y+z*z);
			
			if(up_down == true){
				if(acceleration > acceleration_max)
					acceleration_max = acceleration;
				else if(acceleration <= 9.8)
					up_down = false;
			} else{
				if(acceleration < acceleration_min)
					acceleration_min = acceleration;
				else if(acceleration >= 9.8){
					new_time = new Date().getTime();
					if((new_time - old_time <= 2000) && (acceleration_max - acceleration_min >= 0.1))
						hosu++;
					
					old_time = new_time;
					acceleration_max = (float)9.8;
					acceleration_min = (float)9.8;
					up_down = true;
				}
			}
			
			if(acceleration <= 7.0){
				fall_time1 = new Date().getTime();
				fall_flag1 = true;
			}
			if(fall_flag1 == true){
				fall_jadge1 = new Date().getTime();
				if(fall_jadge1 - fall_time1 > 2000)
					fall_flag1 = false;
			}
			
			if(acceleration - old_acceleration > 0.50){
				fall_time2 = new Date().getTime();
				fall_flag2 = true;
			}
			if(fall_flag2 == true){
				fall_jadge2 = new Date().getTime();
				if(fall_jadge2 - fall_time2 > 2000)
					fall_flag2 = false;
			}
			
			if((fall_flag1 == true) && (fall_flag2 == true) && (hosu >= 3)){
				fall_time3 = new Date().getTime();
				fall_flag3 = true;
			}
			if(fall_flag3 == true){
				fall_jadge3 = new Date().getTime();
				if(fall_jadge3 - fall_time3 > 2000)
					if(Math.abs(acceleration - old_acceleration) > 0.25){
						fall_flag3 = false;
						fall_flag1 = fall_flag2 = false;
					}
				if(fall_jadge3 - fall_time3 > 7000){
					BS2.LightLED(3);
					fall_flag1 = fall_flag2 = fall_flag3 = false;
					
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							tumble_connect();
						}
					}).start();
				}
			}
		}
	}

}
