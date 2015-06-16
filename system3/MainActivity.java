package jp.kagawanct.shigeta2013.familynotice;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static boolean flag1 = false, flag2 = false;

	private final static String TELL = "tel:";
	public final static String SERVER_URL = "192.168.43.91/for_family/for_family.php";

	// 初回開始時間(秒)
	private final static long FIRST_TIME = 5;
	// サーバー読み取り間隔(秒)
	private final static long INTERVAL_TIME = 30;


	private NotificationManager mNoticeM;
	private Button mBtntel;
	private Button mBtnset;
	private Button mBtnnot;
	private Button mBtnmap;
	private TextView mTextState;
	private SharedPreferences mPref;
	private PendingIntent pIntentNIS;
	private AlarmManager alarmManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPref = PreferenceManager.getDefaultSharedPreferences(this);

		mTextState = (TextView) findViewById(R.id.text_state);
		mBtntel = (Button) findViewById(R.id.tellbutton);
		mBtntel.setOnClickListener(this);
		mBtnset = (Button) findViewById(R.id.setingbutton);
		mBtnset.setOnClickListener(this);
		mBtnnot = (Button) findViewById(R.id.stopbutton);
		mBtnnot.setOnClickListener(this);
		mBtnmap = (Button) findViewById(R.id.buttonmap);
		mBtnmap.setOnClickListener(this);
		mNoticeM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		//通知をすべて切る
		mNoticeM.cancelAll();
		
		// プレファレンスの通知フラグを初期化。アプリ初起動時にはフラグを立てる。
		mPref.edit()
				.putBoolean(
						getString(R.string.NotificationFlag_key),
						mPref.getBoolean(
								getString(R.string.NotificationFlag_key), true));

		if (mPref.getBoolean(getString(R.string.NotificationFlag_key), true)) {
			StartIService();
		}
		changeState();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

	private void StartIService() { /*サービスを実行する*/
		Context context = getBaseContext();
		Intent intent = new Intent(context, NoticeIService.class);
		pIntentNIS = PendingIntent.getService(context, -1, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC,
				System.currentTimeMillis(), INTERVAL_TIME * 1000, pIntentNIS);
	}

	private void Tell() {
		String telnum = mPref.getString(getString(R.string.TelNum_key), null);
		if( telnum == null){
			Toast.makeText(MainActivity.this, getString(R.string.tell_error), Toast.LENGTH_SHORT).show();
			return;
		}
		Intent telintent = new Intent(Intent.ACTION_DIAL, Uri.parse(TELL
				+ telnum));
		startActivity(telintent);
	}

	@Override
	public void onClick(View v) { /* ボタン処理 */
		if (v == mBtntel) {
			downState();
			changeState();
			mNoticeM.cancelAll();
			Tell();
		} else if (v == mBtnset) {
			Intent setintent = new Intent(MainActivity.this, pSetingActivity.class);
			startActivity(setintent);
		} else if (v == mBtnnot) {
			clickNotificationBtn();
		} else if (v == mBtnmap) {
			Log.d("ClickMap", "Check1");
			Intent mapintent = new Intent(MainActivity.this, NoticeMapActivity.class);
			startActivity(mapintent);
		}

	}

	public static void setflag1() {
		flag1 = true;
	}

	public static void setflag2() {
		flag2 = true;
	}

	private void changeState() { /*通知ボタンを押したときに実行*/
		if (!mPref.getBoolean(getString(R.string.NotificationFlag_key), true)) {
			mTextState.setText(getString(R.string.state_stop));
			mTextState.setTextColor(Color.BLACK);
		} else if (flag1 && flag2) {
			mTextState.setText(getString(R.string.state_FandL) + "\n"
					+ getString(R.string.state_Tell));
			mTextState.setTextColor(Color.RED);
		} else if (flag1) {
			mTextState.setText(getString(R.string.fall_down) + "\n"
					+ getString(R.string.state_Tell));
			mTextState.setTextColor(Color.RED);
		} else if (flag2) {
			mTextState.setText(getString(R.string.lost) + "\n"
					+ getString(R.string.state_Tell));
			mTextState.setTextColor(Color.RED);
		} else {
			mTextState.setText(getString(R.string.state_none));
			mTextState.setTextColor(Color.BLACK);
		}
		
		if(mPref.getBoolean(getString(R.string.NotificationFlag_key), true)){
			mBtnnot.setText(getString(R.string.Notification_ON));
		} else {
			mBtnnot.setText(getString(R.string.Notification_OFF));
		}
	}

	private void downState() {
		flag1 = false;
		flag2 = false;
	}

	private void clickNotificationBtn() {
		if (mPref.getBoolean(getString(R.string.NotificationFlag_key), true)) {
			alarmManager.cancel(pIntentNIS);
			mNoticeM.cancelAll();
			mPref.edit()
					.putBoolean(getString(R.string.NotificationFlag_key), false)
					.commit();
		} else {
			StartIService();
			mPref.edit()
					.putBoolean(getString(R.string.NotificationFlag_key), true)
					.commit();
		}
		changeState();
	}

}
