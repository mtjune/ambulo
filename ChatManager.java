package jp.kagawanct.shigeta2013.ambulo1;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ChatManager {

	private static final String NAME = "BluetoothEx";
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

	public static final int STATE_NONE = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;

	private BluetoothAdapter btAdapter;
	private int state;
	private AcceptThread acceptT;
	private ConnectThread connectT;
	private ConnectedThread connectedT;

	public ChatManager() {
		this.btAdapter = BluetoothAdapter.getDefaultAdapter();
		this.state = STATE_NONE;
		Log.d("ChatManager", "run Constractar");
	}
	
	private synchronized void setState(int state){
		this.state = state;
	}

	public synchronized int getState() {
		return state;
	}

	public synchronized void start() {
		if (connectT != null) {
			connectT.cancel();
			connectT = null;
		}
		if (connectedT != null) {
			connectedT.cancel();
			connectedT = null;
		}
		if (acceptT == null) {
			acceptT = new AcceptThread();
			acceptT.start();
		}
		setState(STATE_LISTEN);
	}

	public synchronized void connect(BluetoothDevice device) {
		if (state == STATE_CONNECTING) {
			if (connectT != null) {
				connectT.cancel();
				connectT = null;
			}
		}
		if (connectedT != null) {
			connectedT.cancel();
			connectedT = null;
		}
		connectT = new ConnectThread(device);
		connectT.start();
		setState(STATE_CONNECTING);
	}

	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		if (connectT != null) {
			connectT.cancel();
			connectT = null;
		}
		if (connectedT != null) {
			connectedT.cancel();
			connectedT = null;
		}
		if (acceptT != null) {
			acceptT.cancel();
			acceptT = null;
		}
		connectedT = new ConnectedThread(socket);
		connectedT.start();
		setState(STATE_CONNECTED);
	}

	public synchronized void stop() {
		if (connectT != null) {
			connectT.cancel();
			connectT = null;
		}
		if (connectedT != null) {
			connectedT.cancel();
			connectedT = null;
		}
		if (acceptT != null) {
			acceptT.cancel();
			acceptT = null;
		}
		setState(STATE_NONE);
		Log.d("ChatManager", "run StopMethod");
	}


	public synchronized void write(byte[] out) {
		if (state != STATE_CONNECTED)
			return;
		connectedT.write(out);
	}


	private class AcceptThread extends Thread {
		private BluetoothServerSocket serverSocket;

		public AcceptThread() {
			try {
				serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(
						NAME, MY_UUID);
			} catch (Exception e) {
			}
		}

		public void run() {
			BluetoothSocket socket = null;
			while (state != STATE_CONNECTED) {
				try {
					socket = serverSocket.accept();
				} catch (Exception e) {
					break;
				}
				if (socket != null) {
					switch (state) {
					case STATE_LISTEN:
					case STATE_CONNECTING:
						connected(socket, socket.getRemoteDevice());
						break;
					case STATE_NONE:
					case STATE_CONNECTED:
						try {
							socket.close();
						} catch (Exception e) {
						}
						break;
					}
				}
			}
		}

		public void cancel() {
			try {
				serverSocket.close();
			} catch (Exception e) {
			}
		}
	}


	private class ConnectThread extends Thread {
		private BluetoothDevice device;
		private BluetoothSocket socket;

		
		public ConnectThread(BluetoothDevice device) {
			try {
				this.device = device;
				this.socket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (Exception e) {
			}
		}

		
		public void run() {
			btAdapter.cancelDiscovery();
			try {
				socket.connect();
				connectT = null;
				connected(socket, device);
			} catch (Exception e) {
				setState(STATE_LISTEN);
				try {
					socket.close();
				} catch (Exception e2) {
				}
				ChatManager.this.start();
			}
		}

		
		public void cancel() {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}

	
	private class ConnectedThread extends Thread {
		private BluetoothSocket socket;
		private InputStream in;
		private OutputStream out;

		
		public ConnectedThread(BluetoothSocket socket) {
			try {
				this.socket = socket;
				this.in = socket.getInputStream();
				this.out = socket.getOutputStream();
			} catch (Exception e) {
			}
		}

		
		public void run() {
			byte[] buf = new byte[1024];
			int bytes;
			while (true) {
				try {
					bytes = in.read(buf);
				} catch (Exception e) {
					setState(STATE_LISTEN);
					break;
				}
			}
		}

		
		public void write(byte[] buf) {
			try {
				out.write(buf);
			} catch (Exception e) {
			}
		}

		
		public void cancel() {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}

}
