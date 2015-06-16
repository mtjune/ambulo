package jp.kagawanct.shigeta2013.ambulo1;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListActivity extends Activity {

	private final static String BR = System.getProperty("line.separator");
	private final static int MP = LinearLayout.LayoutParams.MATCH_PARENT;
	private final static int WC = LinearLayout.LayoutParams.WRAP_CONTENT;
	
	private final static String DEVICE_NAME = "Galaxy Nexus";
	private final static String DEVICE_ADDRESS = "FC:C7:34:B9:E4:9C";

	private BluetoothAdapter	btAdapter;
	private ArrayAdapter<String>	adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setResult(Activity.RESULT_CANCELED);

		//
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);

		//
		adapter = new ArrayAdapter<String>(this, R.layout.rowdata);

		//
		ListView listView = new ListView(this);
		listView.setLayoutParams(new LinearLayout.LayoutParams(MP, WC));
		listView.setAdapter(adapter);
		layout.addView(listView);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
				//Bluetooth
				btAdapter.cancelDiscovery();
				

				//
				String info = ((TextView)view).getText().toString();
				String address = info.substring(info.length()-17);
				Intent intent = new Intent();
				intent.putExtra("device_address", address);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
			
		});
		Log.d("Layout", "Layout Completed");

		
		//
		IntentFilter filter;
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);

		
		
		//Bluetooth
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if(pairedDevices.size() > 0){
			for(BluetoothDevice device:pairedDevices){
				if( device.getAddress().equals(DEVICE_ADDRESS) )
					adapter.add(getString(R.string.select_device)+BR+device.getAddress());
//				else
//					adapter.add(device.getName()+BR+device.getAddress());
				Log.d(device.getName(), device.getAddress());
			}
		}
		if(btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
	}

	//
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(btAdapter != null) btAdapter.cancelDiscovery();
		this.unregisterReceiver(receiver);
	}


	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		//Bluetooth
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			//Bluetooth
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(device.getBondState() != BluetoothDevice.BOND_BONDED){
					
					if( device.getAddress().equals(DEVICE_ADDRESS) )
						adapter.add(getString(R.string.select_device)+BR+device.getAddress());
					else
						adapter.add(device.getName()+BR+device.getAddress());
				}
			}
			//Bluetooth
			else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device_list, menu);
		return true;
	}

}
