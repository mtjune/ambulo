package jp.kagawanct.shigeta2013.familynotice;

import java.net.MalformedURLException;
import java.net.URL;

import jp.kagawanct.shigeta2013.familynotice.R;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class NoticeIService extends IntentService {

	

	private Notification mNotice1, mNotice2;
	private NotificationManager mNoticeM;
	private SharedPreferences mPref;
	
	private URL url;

	
	public NoticeIService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public NoticeIService() {
		super("NoticeIService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//定期処理開始
		 try {
			url = new URL("192.168.43.91/for_family/for_family.php");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		Log.d("TAG", "TEIKISHORI");
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean flag1 = false, flag2 = false;
		/* ここにサーバーに読みにいく処理 */
		flag1 = true;
		flag2 = true;//仮に

		
		if(flag1) { /*転倒*/
			Notice1(mPref.getString(getString(R.string.UserName_key), null));
			MainActivity.setflag1();
		}
		
		if(flag2){ /*迷子*/
			Notice2(mPref.getString(getString(R.string.UserName_key), null));
			MainActivity.setflag2();
		}
		flag1=false;
		flag2=false;

		//定期処理ここまで
	}
	private void Notice1(String name) {
		mNotice1 = new Notification();
		mNotice1.icon = R.drawable.notification2;
		mNotice1.tickerText = getString(R.string.fall_down);

		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		mNotice1.setLatestEventInfo(getApplicationContext(),
				getString(R.string.fall_down), name + " "
						+ getString(R.string.fall_down_sentence), pi);

		mNoticeM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNoticeM.notify(1, mNotice1);

	}

	private void Notice2(String name) {
		mNotice2 = new Notification();
		mNotice2.icon = R.drawable.notification2;
		mNotice2.tickerText = getString(R.string.lost);

		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		mNotice2.setLatestEventInfo(getApplicationContext(),
				getString(R.string.lost), name + " "
						+ getString(R.string.lost_sentence), pi);

		mNoticeM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNoticeM.notify(2, mNotice2);

	}

}
