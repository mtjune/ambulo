package com.example.ambulo1;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class ChatManager {

	//髫ｪ�ｭ陞ｳ螢ｼ�ｮ螢ｽ辟�
	private static final String NAME = "BluetoothEx";
	private static final UUID	MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

	//霑･�ｶ隲ｷ蜿･�ｮ螢ｽ辟�
	public static final int STATE_NONE = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;

	//陞溽判辟�
	private BluetoothAdapter btAdapter;
	private int state;
	private Handler handler;
	private AcceptThread acceptT;
	private ConnectThread connectT;
	private ConnectedThread connectedT;

	//郢ｧ�ｳ郢晢ｽｳ郢ｧ�ｹ郢晏現ﾎ帷ｹｧ�ｯ郢ｧ�ｿ
	public ChatManager(Handler handler){
		this.btAdapter = BluetoothAdapter.getDefaultAdapter();
		this.state = STATE_NONE;
		this.handler = handler;
		Log.d("ChatManager", "run Constractar");
	}

	//霑･�ｶ隲ｷ荵晢ｿｽ隰厄ｿｽ�ｮ�ｽ
	private synchronized void setState(int state){
		this.state = state;
		handler.obtainMessage(BTtoSerial.MSG_STATE_CHANGE, state, -1).sendToTarget();
	}

	//霑･�ｶ隲ｷ荵晢ｿｽ陷ｿ髢�ｽｾ�ｽ
	public synchronized int getState(){
		return state;
	}

	//郢ｧ�ｵ郢晢ｽｼ郢晁��ｽ隰暦ｽ･驍ｯ螢ｼ�ｾ�ｽ笆�ｹｧ�ｹ郢晢ｽｬ郢晢ｿｽ繝ｩ邵ｺ�ｮ鬮｢蜿･�ｧ�ｽ
	public synchronized void start(){
		if(connectT != null){
			connectT.cancel();
			connectT = null;
		}
		if(connectedT != null){
			connectedT.cancel();
			connectedT = null;
		}
		if(acceptT == null){
			acceptT = new AcceptThread();
			acceptT.start();
		}
		setState(STATE_LISTEN);
	}

	//郢ｧ�ｯ郢晢ｽｩ郢ｧ�､郢ｧ�｢郢晢ｽｳ郢晏現�ｽ隰暦ｽ･驍ｯ螟奇ｽｦ竏ｵ�ｱ繧�○郢晢ｽｬ郢晢ｿｽ繝ｩ鬮｢蜿･�ｧ�ｽ
	public synchronized void connect(BluetoothDevice device){
		if(state == STATE_CONNECTING){
			if(connectT != null){
				connectT.cancel();
				connectT = null;
			}
		}
		if(connectedT != null){
			connectedT.cancel();
			connectedT = null;
		}
		connectT = new ConnectThread(device);
		connectT.start();
		setState(STATE_CONNECTING);
	}

	//隰暦ｽ･驍ｯ螢ｻ�ｸ�ｭ邵ｺ�ｮ陷�ｽｦ騾�ｿｽ縺帷ｹ晢ｽｬ郢晢ｿｽ繝ｩ邵ｺ�ｮ鬮｢蜿･�ｧ�ｽ
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
		if(connectT != null){
			connectT.cancel();
			connectT = null;
		}
		if(connectedT != null){
			connectedT.cancel();
			connectedT = null;
		}
		if(acceptT != null){
			acceptT.cancel();
			acceptT = null;
		}
		connectedT = new ConnectedThread(socket);
		connectedT.start();
		setState(STATE_CONNECTED);
	}

	//郢ｧ�ｹ郢晢ｽｬ郢晢ｿｽ繝ｩ邵ｺ�ｮ陋帶㊧�ｭ�｢
	public synchronized void stop(){
		if(connectT != null){
			connectT.cancel();
			connectT = null;
		}
		if(connectedT != null){
			connectedT.cancel();
			connectedT = null;
		}
		if(acceptT != null){
			acceptT.cancel();
			acceptT = null;
		}
		setState(STATE_NONE);
		Log.d("ChatManager", "run StopMethod");
	}

	//鬨ｾ竏ｽ�ｿ�｡郢晢ｿｽ�ｽ郢ｧ�ｿ邵ｺ�ｮ隴厄ｽｸ邵ｺ蟠趣ｽｾ�ｼ邵ｺ�ｿ
	public synchronized void write(byte[] out){
		if(state != STATE_CONNECTED) return;
		connectedT.write(out);
	}

	//郢ｧ�ｵ郢晢ｽｼ郢晁��ｽ隰暦ｽ･驍ｯ螢ｼ�ｾ�ｽ笆�ｹｧ�ｹ郢晢ｽｬ郢晢ｿｽ繝ｩ
	private class AcceptThread extends Thread{
		private BluetoothServerSocket serverSocket;
		//郢ｧ�ｳ郢晢ｽｳ郢ｧ�ｹ郢晏現ﾎ帷ｹｧ�ｯ郢ｧ�ｿ
		public AcceptThread(){
			try{
				serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch(Exception e){
			}
		}

		//陷�ｽｦ騾�ｿｽ
		public void run(){
			BluetoothSocket socket = null;
			while (state != STATE_CONNECTED){
				try{
					socket = serverSocket.accept();
				} catch(Exception e){
					break;
				}
				if(socket != null){
					switch(state){
					case STATE_LISTEN:
					case STATE_CONNECTING:
						connected(socket, socket.getRemoteDevice());
						break;
					case STATE_NONE:
					case STATE_CONNECTED:
						try{
							socket.close();
						} catch(Exception e){
						}
						break;
					}
				}
			}
		}

		//郢ｧ�ｭ郢晢ｽ｣郢晢ｽｳ郢ｧ�ｻ郢晢ｽｫ
		public void cancel(){
			try{
				serverSocket.close();
			}catch(Exception e){
			}
		}
	}

	//郢ｧ�ｯ郢晢ｽｩ郢ｧ�､郢ｧ�｢郢晢ｽｳ郢晏沺逎�け螟奇ｽｦ竏ｵ�ｱ繧�○郢晢ｽｬ郢晢ｿｽ繝ｩ
	private class ConnectThread extends Thread {
		private BluetoothDevice device;
		private BluetoothSocket socket;

		//郢ｧ�ｳ郢晢ｽｳ郢ｧ�ｹ郢晏現ﾎ帷ｹｧ�ｯ郢ｧ�ｿ
		public ConnectThread(BluetoothDevice device){
			try{
				this.device = device;
				this.socket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch(Exception e) {
			}
		}

		//陷�ｽｦ騾�ｿｽ
		public void run(){
			btAdapter.cancelDiscovery();
			try{
				socket.connect();
				connectT = null;
				connected(socket, device);
			} catch(Exception e) {
				setState(STATE_LISTEN);
				try {
					socket.close();
				} catch(Exception e2) {
				}
				ChatManager.this.start();
			}
		}

		//郢ｧ�ｭ郢晢ｽ｣郢晢ｽｳ郢ｧ�ｻ郢晢ｽｫ
		public void cancel(){
			try{
				socket.close();
			} catch(Exception e) {
			}
		}
	}

	//隰暦ｽ･驍ｯ螢ｻ�ｸ�ｭ邵ｺ�ｮ陷�ｽｦ騾�ｿｽ縺帷ｹ晢ｽｬ郢晢ｿｽ繝ｩ
	private class ConnectedThread extends Thread{
		private BluetoothSocket socket;
		private InputStream in;
		private OutputStream out;

		//郢ｧ�ｳ郢晢ｽｳ郢ｧ�ｹ郢晏現ﾎ帷ｹｧ�ｯ郢ｧ�ｿ
		public ConnectedThread(BluetoothSocket socket){
			try{
				this.socket = socket;
				this.in =socket.getInputStream();
				this.out = socket.getOutputStream();
			} catch (Exception e) {
			}
		}

		//陷�ｽｦ騾�ｿｽ
		public void run(){
			byte[] buf = new byte[1024];
			int bytes;
			while(true){
				try{
					bytes = in.read(buf);
					handler.obtainMessage(BTtoSerial.MSG_DEN, bytes, -1, buf).sendToTarget();
				} catch(Exception e) {
					setState(STATE_LISTEN);
					break;
				}
			}
		}

		//隴厄ｽｸ邵ｺ蟠趣ｽｾ�ｼ邵ｺ�ｿ
		public void write(byte[] buf){
			try{
				out.write(buf);
			} catch(Exception e){
			}
		}

		//郢ｧ�ｭ郢晢ｽ｣郢晢ｽｳ郢ｧ�ｻ郢晢ｽｫ
		public void cancel(){
			try{
				socket.close();
			} catch(Exception e){
			}
		}
	}
}
