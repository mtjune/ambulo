package jp.kagawanct.shigeta2013.ambulo1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "OCVSample::Activity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	public static final int JAVA_DETECTOR = 0;
	public static final int NATIVE_DETECTOR = 1;
	public static final int MEDNUM = 3;
	public static final int MEDOUT = MEDNUM/2;

	private MenuItem mItemFace50;
	private MenuItem mItemFace40;
	private MenuItem mItemFace30;
	private MenuItem mItemFace20;
	private MenuItem mItemType;

	private Mat mRgba;
	private Mat mGray;
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private DetectionBasedTracker mNativeDetector;
	
	private BluetoothAdapter btAdapter;
	private ChatManager chatManager;
	
	final private String signal = "d";
	
	public static final int MSG_STATE_CHANGE = 1;
	public static final int MSG_READ = 2;

	private static final int RQ_CONNECT_DEVICE = 1;
	private static final int RQ_ENABLE_BT = 2;
	
	

	private int mDetectorType = JAVA_DETECTOR;
	private String[] mDetectorName;

	private float mRelativeFaceSize = 0.1f;
	private int mAbsoluteFaceSize = 0;

	private CameraBridgeViewBase mOpenCvCameraView;

	private LoopNum looping;
	private LoopNum looping2;
	private TextView indicate;
	private Button btntest;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				
				// Load native library after(!) OpenCV initialization
				System.loadLibrary("detection_based_tracker");

				try {
					// load cascade file from application resources
					
					InputStream is = getResources().openRawResource(R.raw.car);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					Log.d("CVLOAD", "check1");
					
					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();
					Log.d("CVLOAD", "check2");

					mJavaDetector = new CascadeClassifier(
							mCascadeFile.getAbsolutePath());
					
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());
					Log.d("CVLOAD", "check3");

					mNativeDetector = new DetectionBasedTracker(
							mCascadeFile.getAbsolutePath(), 0);
					

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
				
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
			Log.i(TAG, "OpenCV loaded successfully2");
		}
		
	};

	public MainActivity() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
		
		

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Log.d("onCreate", "check1");
		setContentView(R.layout.activity_main_surfaceview);
		
		
		indicate = (TextView)findViewById(R.id.Text);
		Log.d("onCreate", "check1");
//		btntest = (Button)findViewById(R.id.buttonDEN); /*テスト用ボタン*/
//		Btntest.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				indicate.setText(indicate.getText().toString() + "1");
//				chatManager.write(signal.getBytes());
//			}
//		});
		looping = new LoopNum();
		looping2 = new LoopNum();
		

		
		Log.d("onCreate", "check2");
		
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_main_surfaceview);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		Log.d("onCreate", "check3");
		
		//Bluetoothの設定
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		chatManager = new ChatManager();
		Intent intent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(intent, RQ_CONNECT_DEVICE);
		
		Log.d("onCreate", "check perfect");
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
		if (chatManager.getState() == ChatManager.STATE_NONE) {
			chatManager.start();
		}
	}
	@Override
	public void onStart() {
		super.onStart();
		if (!btAdapter.isEnabled()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, RQ_ENABLE_BT);
		}
	}

	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
		chatManager.stop();
	}

	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
		
	}

	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		int sizetotal = 0;
		int[] median = new int[MEDNUM];

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
			mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
		}

		MatOfRect faces = new MatOfRect();

		Log.d("onCameraFrame", "Native.detect check1");
		
		if (mNativeDetector != null)
			mNativeDetector.detect(mGray, faces);
		
		Log.d("onCameraFrame", "Native.detect check2");
		
		sizetotal = 0;
		Rect[] facesArray = faces.toArray();
		Log.d("onCameraFrame", "Native.detect check3");
		for (int i = 0; i < facesArray.length; i++) {
			Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
					FACE_RECT_COLOR, 3);
			sizetotal += facesArray[i].height;
		}
		Log.d("onCameraFrame", "Native.detect check4");
		looping.pushdata(sizetotal);
		Log.d("onCameraFrame", "Native.detect check5");

		if ((looping.getnum() % MEDNUM) == 0) {
			boolean[] flag = new boolean[4];
			for (int i = 0; i < MEDNUM; i++) {
				median[i] = looping.popdata(i);
			}
			looping2.pushdata(outmedian(median));

			flag[0] = (looping2.popdata(0) > looping2.popdata(1));
			flag[1] = (looping2.popdata(1) > looping2.popdata(2));
			flag[2] = (looping2.popdata(2) > looping2.popdata(3));
			flag[3] = (looping2.popdata(3) > looping2.popdata(4));

			if (flag[0] && flag[1] ) {
				//ここから危険信号
				indicate.setText(indicate.getText().toString() + "T");
				chatManager.write(signal.getBytes());
				
				
				//ここまで危険信号
			}

		}
		
		Log.d("onCameraFrame", "Native.detect check Perfect");

		return mRgba;
	}

	private int outmedian(int[] med) { // MEDNUM = 3
		boolean[] flag = new boolean[MEDNUM];
		int[] value = new int[MEDNUM];
		int maxvalue = med[0], hold = 0;
		for (int i = 0; i < MEDNUM; i++) {
			flag[i] = true;
		}

		for (int i = 0; i <= MEDOUT; i++) { // MEDNUM = 3
			for (int j = 0; j < MEDNUM; j++) {
				if (flag[j] && (maxvalue < med[j])) {
					maxvalue = med[j];
					hold = j;
				}
			}
			flag[hold] = false;
			value[i] = maxvalue;
			maxvalue = 0;
		}

		return value[MEDOUT];
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemFace50 = menu.add("Face size 50%");
		mItemFace40 = menu.add("Face size 40%");
		mItemFace30 = menu.add("Face size 30%");
		mItemFace20 = menu.add("Face size 20%");
		mItemType = menu.add(mDetectorName[mDetectorType]);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
//		if (item == mItemFace50)
//			setMinFaceSize(0.5f);
//		else if (item == mItemFace40)
//			setMinFaceSize(0.4f);
//		else if (item == mItemFace30)
//			setMinFaceSize(0.3f);
//		else if (item == mItemFace20)
//			setMinFaceSize(0.2f);
//		else if (item == mItemType) {
//			mDetectorType = (mDetectorType + 1) % mDetectorName.length;
//			item.setTitle(mDetectorName[mDetectorType]);
//			setDetectorType(mDetectorType);
//		}
		return true;
	}

	private void setMinFaceSize(float faceSize) {
		mRelativeFaceSize = faceSize;
		mAbsoluteFaceSize = 0;
	}

	private void setDetectorType(int type) {
		if (mDetectorType != type) {
			mDetectorType = type;

			if (type == NATIVE_DETECTOR) {
				Log.i(TAG, "Detection Based Tracker enabled");
				mNativeDetector.start();
			} else {
				Log.i(TAG, "Cascade detector enabled");
				mNativeDetector.stop();
			}
		}
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RQ_CONNECT_DEVICE) {
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString("device_address");
				chatManager.connect(btAdapter.getRemoteDevice(address));
			}
		}
		else if (requestCode == RQ_ENABLE_BT) {
			if (resultCode != Activity.RESULT_OK) {
				finish();
			}
		}
		indicate.setText(indicate.getText().toString() + chatManager.getState());
	}

}
