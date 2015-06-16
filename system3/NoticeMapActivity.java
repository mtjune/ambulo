package jp.kagawanct.shigeta2013.familynotice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NoticeMapActivity extends FragmentActivity {

	private static final String TAG = "NoticeMapActivity";

	private GoogleMap mMap;
	private SharedPreferences mPref;

	@Override
	public void onCreate(Bundle savedKnstanceState) {
		super.onCreate(savedKnstanceState);
		setContentView(R.layout.activity_noticemap);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		Log.d(TAG, "onCreate Check1");
		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mapviewf)).getMap();
		Log.d(TAG, "onCreate Check2");
		
		
		LatLng positionNow = new LatLng(34.309714, 134.010715); /*現在位置*/

		Log.d(TAG, "onCreate Check3");
//		mMap.moveCamera(CameraUpdateFactory
//				.newCameraPosition(new CameraPosition.Builder()
//						.target(positionNow).zoom(16.0f)
//						.build()));
		
//		CameraPosition mCameraP = new CameraPosition(positionNow, 16.0f, 0, 0);
//		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraP));
		Log.d(TAG, "onCreate Check4");
//		mMap.addMarker(
//				new MarkerOptions().position(positionNow))
//				.setTitle(mPref.getString(getString(R.string.UserName_key), "") + "さん");

		Log.d(TAG, "onCreate Check5");
		

	}
}
