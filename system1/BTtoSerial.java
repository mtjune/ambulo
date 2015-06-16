package com.example.ambulo1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class BTtoSerial {
	public static final int MSG_STATE_CHANGE = 1;
	public static final int MSG_DEN = 2;

	public static final int RQ_CONNECT_DEVICE = 1;
	public static final int RQ_ENABLE_BT = 2;

	private BluetoothAdapter btAdapter;
	private ChatManager chatManager;
	private UsbManager manager;
	private UsbSerialDriver usb;

	private TextView TState;

	// Constractor(Accept TextView & Handler & UsbManager)
	public BTtoSerial(TextView textview, Handler handler, UsbManager mana) {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		chatManager = new ChatManager(handler);

		manager = mana;
		usb = UsbSerialProber.acquire(manager);
		if (usb != null) {
			try {
				usb.open();
				usb.setBaudRate(9600);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		TState = textview;
	}

	public void AcceptHandlerMessage(Message msg) {
		if (msg.what == MSG_STATE_CHANGE) {
			switch (msg.arg1) {
			case ChatManager.STATE_CONNECTED:
				TState.setText("Connected");
				break;
			case ChatManager.STATE_CONNECTING:
				TState.setText("Connecting");
				break;
			case ChatManager.STATE_LISTEN:
			case ChatManager.STATE_NONE:
				TState.setText("None");
				break;
			}
		} else if (msg.what == MSG_DEN) {
				LightLED(1);
		}
	}

	public void BActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RQ_CONNECT_DEVICE) {
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString("device_address");
				chatManager.connect(btAdapter.getRemoteDevice(address));
			}
		}
		else if (requestCode == RQ_ENABLE_BT) {
			if (resultCode != Activity.RESULT_OK) {
				TState.setText("Bluetooth is not found!");
			}
		}
	}

	public void BClick() {
		LightLED(4);

	}

	public synchronized void BResume() {
		if (chatManager.getState() == ChatManager.STATE_NONE) {
			chatManager.start();
		}
	}

	public void BDestroy() {
		chatManager.stop();
		try {
			usb.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean EnBTA() {
		return btAdapter.isEnabled();
	}

	public int SmBTA() {
		return btAdapter.getScanMode();
	}

	// Bluetooth to Serial touch
	public void LightLED(int num) {
		switch (num) {
		case 0:
			try {
				usb.write("0".getBytes("UTF-8"), 1);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 1:
			try {
				usb.write("1".getBytes("UTF-8"), 1);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			try {
				usb.write("2".getBytes("UTF-8"), 1);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 3:
			try {
				usb.write("3".getBytes("UTF-8"), 1);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 4:
			try {
				usb.write("4".getBytes("UTF-8"), 1);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
		
		
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public Intent ensureDiscoverable() {
		if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent intent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			return intent;
		}
		return null;
		
	}

}
