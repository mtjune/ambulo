package com.example.ambulo1;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static String SERVER_URL = "http://192.168.43.91";
	private static final String SERIAL_NUMBER = "19940214";
	private static final String URL_FIRST_CONTACT = "/first_contact.php/first_contact.php";
	
	private TextView sNumber;
	private TextView ID;
	private TextView AMBULO_state;
	private TextView lblReceive;
	private Button button_run;
	private Button button_stop;
	private EditText edit1;
	private TextView test;
	
	private boolean ambulo_state = false;
	private String Decided_URL;
	private Intent intent = null;
	final Handler handler2 = new Handler();
	
	public static BTtoSerial BS;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			BS.AcceptHandlerMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sNumber = (TextView) findViewById(R.id.textView_sNumber);
		ID = (TextView) findViewById(R.id.textView_ID);
		AMBULO_state = (TextView)findViewById(R.id.textView_AMBULO_state);
		lblReceive = (TextView)findViewById(R.id.textView_BlueTooth_state);	
		test = (TextView) findViewById(R.id.textView_hosu);
		
		button_run = (Button)findViewById(R.id.button_RUN);
		button_stop = (Button)findViewById(R.id.button_STOP);
		
		intent = new Intent(this, amblo1_service.class);
		BS = new BTtoSerial(lblReceive, handler,(UsbManager) getSystemService(USB_SERVICE));
		
		SharedPreferences pref = getSharedPreferences("AMBULO1_pref", MODE_PRIVATE);
		Editor editor = pref.edit();
		
		editor.putString("sNumber", SERIAL_NUMBER);
		editor.commit();
		
		SERVER_URL = pref.getString("server_URL",  SERVER_URL);
		test.setText(SERVER_URL);
		
		sNumber.setText(pref.getString("sNumber",  "Not_set"));
		
		if (pref.getBoolean("ID_set", false) == false)
			SettingDialog();
		else
			ID.setText(pref.getString("ID",  "Not_set"));
		
		button_run.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(ambulo_state == false){
					ambulo_state = true;
					AMBULO_state.setText(R.string.State_Run);
					startService(intent);
				}
			}
		});
		
		button_stop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(ambulo_state == true){
					ambulo_state = false;
					AMBULO_state.setText(R.string.State_Stop);
					stopService(intent);
					BS.LightLED(0);
				}
			}
		});
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (!BS.EnBTA()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, BTtoSerial.RQ_ENABLE_BT);
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		BS.BResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		BS.LightLED(0);
		BS.BDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e){
		if(keyCode == KeyEvent.KEYCODE_BACK){
			new AlertDialog.Builder(this)
			.setTitle(R.string.EndDialog_Title)
			.setMessage(R.string.EndDialog_Message)
			.setPositiveButton(R.string.Dialog_Yes,  new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					android.util.Log.v("location", "removeUpdates_button_back");
					stopService(intent);
					finish();
				}
			})
			.setNegativeButton(R.string.Dialog_No,  new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
			return true;
		}
		else
			return false;
	}
	
private void SettingDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.SettingDialog_Title)
				.setMessage(R.string.SettingDialog_Message)
				.setPositiveButton(R.string.Dialog_Yes, new DialogInterface.OnClickListener() {
					
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								new Thread(new Runnable() {
									
									@Override
									public void run() {
										setID();
										handler2.post(new Runnable() {

											@Override
											public void run() {
												SharedPreferences pref = getSharedPreferences("AMBULO1_pref", MODE_PRIVATE);
												if (pref.getBoolean("ID_set", false) == false)
													fail_setID_Dialog();
												else
													ID.setText(pref.getString("ID", "Not_set"));
											}
											
										});
									}
									
								}).start();
								
							}
							
						})
				.setNegativeButton(R.string.Dialog_No, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
					
				}).show();
		
	}

private void fail_setID_Dialog(){
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle(R.string.fail_setID_Dialog_Title)
		.setMessage(R.string.fail_setID_Dialog_Message)
		.setPositiveButton(R.string.Dialog_Yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						setID();
						handler2.post(new Runnable() {

							@Override
							public void run() {
								SharedPreferences pref = getSharedPreferences("AMBULO1_pref", MODE_PRIVATE);
								if (pref.getBoolean("ID_set", false) == false)
									fail_setID_Dialog();
								else
									ID.setText(pref.getString("ID", "Not_set"));
							}
							
						});
					}
					
				}).start();
				
			}
			
		})
		.setNegativeButton(R.string.Dialog_No,  new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
			
		}).show();
}

private void setID() {
	SharedPreferences pref = getSharedPreferences("AMBULO1_pref", MODE_PRIVATE);
	Editor editor = pref.edit();
	
	StringBuffer strBuff = new StringBuffer(	SERVER_URL);
	strBuff.append(URL_FIRST_CONTACT)
			.append("?sNumber=")
			.append(pref.getString("sNumber", "Not_set"));
	
	Decided_URL = strBuff.toString();
	
	HttpURLConnection httpCon = null;
	InputStream in = null;

	try {
		URL url = new URL(Decided_URL);
		httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setRequestMethod("GET");
		httpCon.connect();
		
		android.util.Log.v("setID", "Http connect");

		in = httpCon.getInputStream();

		String id = new String();
		byte[] line = new byte[1024];
		int size;

		while (true) {
			size = in.read(line);
			if (size <= 0)
				break;
			id += new String(line);
		}

		editor.putString("ID", id);
		editor.putBoolean("ID_set", true);
		editor.commit();

	} catch (Exception e) {
		e.printStackTrace();
		android.util.Log.v("setID", " Http error1");
	} finally {
		try {
			if (httpCon != null)
				httpCon.disconnect();
			if (in != null)
				in.close();
		} catch (Exception e) {
			e.printStackTrace();
			android.util.Log.v("setID", "Http error2");
		}
	}
	
}

private void setURL_Dialog(){
	 edit1 = new EditText(this);
	new AlertDialog.Builder(this)
				.setTitle("URL setting")
				.setView(edit1)
				.setPositiveButton("set", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(edit1.getText().length() != 0){
							String set_url;
							set_url = edit1.getText().toString();
							SharedPreferences pref = getSharedPreferences("AMBULO1_pref", MODE_PRIVATE);
							Editor editor = pref.edit();
							
							editor.putString("server_URL", set_url);
							editor.commit();
							
							SERVER_URL = set_url;
						}
					}
				}).show();
	
}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
	
	switch(item.getItemId()){
	case R.id.item1 : 
		if (BS.ensureDiscoverable() != null)
			startActivity(BS.ensureDiscoverable());
		return true;
	case R.id.item2 :
		setURL_Dialog();
	}
	
	return false;
}

public void onActivityResult(int requestCode, int resultCode, Intent data) {
	BS.BActivityResult(requestCode, resultCode, data);
}

}