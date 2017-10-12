/** ============================================================== */
package com.THLight.USBeacon.Sample.ui;
/** ============================================================== */

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.THLight.USBeacon.App.Lib.BatteryPowerData;
import com.THLight.USBeacon.App.Lib.USBeaconConnection;
import com.THLight.USBeacon.App.Lib.USBeaconServerInfo;
import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;
import com.THLight.USBeacon.Sample.DecideConnect;
import com.THLight.USBeacon.Sample.R;
import com.THLight.USBeacon.Sample.ScanediBeacon;
import com.THLight.USBeacon.Sample.THLApp;
import com.THLight.USBeacon.Sample.THLConfig;
import com.THLight.Util.THLLog;
import com.ypcloud.app2web.App2WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** ============================================================== */
public class UIMain extends Activity implements LocationListener, iBeaconScanManager.OniBeaconScan
{
	/** this UUID is generate by Server while register a new account. */
	final UUID QUERY_UUID		= UUID.fromString("BB746F72-282F-4378-9416-89178C1019FC");
	/** server http api url. */
	final String HTTP_API		= "http://www.usbeacon.com.tw/api/func";

	static String STORE_PATH	= Environment.getExternalStorageDirectory().toString()+ "/USBeaconSample/";

	final int REQ_ENABLE_BT		= 2000;
	final int REQ_ENABLE_WIFI	= 2001;

	final int MSG_SCAN_IBEACON			= 1000;
	final int MSG_UPDATE_BEACON_LIST	= 1001;
	final int MSG_START_SCAN_BEACON		= 2000;
	final int MSG_STOP_SCAN_BEACON		= 2001;
	final int MSG_SERVER_RESPONSE		= 3000;

	// final int TIME_BEACON_TIMEOUT		= 30000;

	// Declaration
	public double[] RSSI_Avg = { 0.0, 0.0, 0.0, 0.0 };			//	Average array to store the averaged RSSI. Average serves as the preprocessing
	public int[] RSSI_Cnt = { 0, 0, 0, 0 };			//	Count how many RSSI signal we get
	public int[] RSSI_Sum = { 0, 0, 0, 0 };			//	Sum up all the RSSI signal and then we can do average
	public int[] RSSI_Raw = { -120, -120, -120 ,-120 };
	public int[] RSSI_RawSum = { 0, 0, 0 ,0 };
	public final int TIME_BEACON_TIMEOUT = 2000;	//  Remove the disappeared Beacon from the ListView every (TIME_BEACON_TIMEOUT / 1000) seconds
	//	public int CntToCloseApp = 1;					// 	The count to close the app, when we want to count to 500, we count from 1 to 500, we get 500 counts
	public int CntOverflowFlag = 0;					//  When we count to CntLimit, it means it's time to average the sum and send the RSSI to server
	public int getBeaconFlag = 0;
	public int RSSICntLimit = 30;					//	When we count to RSSICntLimit, we average the sum
	public String kNNTimeStamp = "2.6";
	public String timeFileName = "2.6";
	public int kNNkVal = 12;
	public int rawDataNum = 910;//13298   Raw Data+1
	public int getData=-1,getReData=-1;

	//	public int CloseAppLimit = 500;					//	When we count to CloseAppLimit, we close app. CloseAppLimit means how many data we want to collect
	public String returnArea;
	// Determine that you are indoor or outdoor
//	int RSSI_InOutDoor = -200;						// Store received RSSI value and to recognize whether the Beacon is null or not; Use "-200"(just choose any number less than -100) to represent it's outdoor cuz "0" has its meaning : RSSI value = 0
//	int minor_InOutDoor;

	// Define Major number, so we just need to modify the Major number here, no need to modify in the case statement
	final int MajorTHLightBeacon = 7051;
	//final int MajorTHLightBeacon = 7;

	// Define Minor number, so we just need to modify the Minor number here, no need to modify in the case statement
	//EE1F
	final int MinorA = 1;
	final int MinorB = 2;
	final int MinorC = 3;
	final int MinorD = 4;

	// Define the zone name
	String Zone = "zone-EE1";

	private static Toast toast;
	private static TextView toastText;
	// Declare an ArrayList to store RSSI_Avg and store in the local storage as a Text file
	/*ArrayList<Integer> StoreList = new ArrayList<Integer>();
	public int CntTxtNewLine = 0;

	// Declare a String to store Column(Feature)
	String FeatureColumn = "Beacon1,Beacon2,Beacon3"; */

	THLApp App		= null;
	THLConfig Config= null;

	BluetoothAdapter mBLEAdapter= BluetoothAdapter.getDefaultAdapter();

	/** scaner for scanning iBeacon around. */
	iBeaconScanManager miScaner	= null;



	List<ScanediBeacon> miBeacons	= new ArrayList<ScanediBeacon>();

	// ======================  Send to SQL -- begin  =========================== //

	public String destinationIp = "http://www.oort.com.tw/bohan/carryMe/";		// For EE 1st floor
	public String destination1 = "/cgi-bin/";		// For EE 1st floor
	public String destination2 = "_CGI.py";		// For EE 1st floor
	public String destinationClassfier = "KNN";		// For EE 1st floor
	private URL url;
	private HttpURLConnection client;
	private JSONObject response;
	JSONObject request = new JSONObject();
	public Handler myHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what)
			{
				case 1:
					String json = null;
					try {
						json = (String) response.get("message");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Toast.makeText(UIMain.this, json, Toast.LENGTH_LONG).show();
					break;
			}
			return false;
		}
	});
	// ======================  Send to SQL -- end  ============================= //

	public ConnectivityManager conns;// test�s�u
	//final static private String _DEFINE_WEBAPP_URL = "inner2.html" ;//http://www.oort.com.tw/bohan/googlePos/inner2.html
	//final static private String _DEFINE_WEBAPP_URL = "http://www.oort.com.tw/bohan/googlePos/" ;//http://www.oort.com.tw/bohan/googlePos/inner2.html
	final static private String _DEFINE_WEBAPP_URL = "http://www.oort.com.tw/bohan/carryMe/";//http://www.oort.com.tw/bohan/googlePos/inner2.html
	public App2WebView wv; // �s�u��WebView
	public DecideConnect decideConnect; // decide connect
	private boolean re_entry = false;
	public String JudgePortraitLandscape = ""; // Judge portrait landscape

	RelativeLayout L1;
	private ProgressBar probar; //loading bar
	public ImageView im;// loading_background_white
	public VideoView vv; // Boot video

	// Flag for GPS status
	boolean isGPSEnabled = false;
	// Flag for network status
	boolean isNetworkEnabled = false;
	// Flag for GPS status
	boolean canGetLocation = false;
	Location location; // Location
	double latitude; // Latitude
	double longitude; // Longitude
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
	// Declaring a Location Manager
	protected LocationManager locationManager;


	/** ================================================ */
	Handler mHandler= new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case MSG_SCAN_IBEACON:
				{
					int timeForScaning		= msg.arg1;
					int nextTimeStartScan	= msg.arg2;

					miScaner.startScaniBeacon(timeForScaning);
					this.sendMessageDelayed(Message.obtain(msg), nextTimeStartScan);
				}
				break;

				case MSG_SERVER_RESPONSE:
					switch(msg.arg1)
					{
						case USBeaconConnection.MSG_NETWORK_NOT_AVAILABLE:
							break;

						case USBeaconConnection.MSG_HAS_NO_UPDATE:
							Toast.makeText(UIMain.this, "No new BeaconList.", Toast.LENGTH_SHORT).show();
							break;

						case USBeaconConnection.MSG_DOWNLOAD_FINISHED:
							break;

						case USBeaconConnection.MSG_DOWNLOAD_FAILED:
							Toast.makeText(UIMain.this, "Download file failed!", Toast.LENGTH_SHORT).show();
							break;

						case USBeaconConnection.MSG_DATA_UPDATE_FAILED:
							Toast.makeText(UIMain.this, "UPDATE_FAILED!", Toast.LENGTH_SHORT).show();
							break;
					}
					break;
			}
		}
	};
	long ReStartTime,StartTime,EndTime,ProcessTime;
	//int MeanFilterTime=5;
	int RSSI_TimeCnt=0;
	/** ================================================ */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.ui_main);
		setContentView(R.layout.activity_main);
		//StartTime=System.currentTimeMillis();
		App		= THLApp.getApp();
		Config	= THLApp.Config;
		ReStartTime=System.currentTimeMillis();
		/** create instance of iBeaconScanManager. */
		miScaner		= new iBeaconScanManager(this, this);

		//mLVBLE			= (ListView)findViewById(R.id.beacon_list);
		//mLVBLE.setAdapter(mListAdapter);

		if(!mBLEAdapter.isEnabled())
		{
			Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQ_ENABLE_BT);
		}
		else
		{
			Message msg= Message.obtain(mHandler, MSG_SCAN_IBEACON, 1000, 1100);
			msg.sendToTarget();
		}

		/** create store folder. */
		File file= new File(STORE_PATH);
		if(!file.exists())
		{
			if(!file.mkdirs())
			{
				Toast.makeText(this, "Create folder("+ STORE_PATH+ ") failed.", Toast.LENGTH_SHORT).show();
			}
		}

		/** check network is available or not. */
		ConnectivityManager cm	= (ConnectivityManager)getSystemService(UIMain.CONNECTIVITY_SERVICE);
		if(null != cm)
		{
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if(null == ni || (!ni.isConnected()))
			{
				//dlgNetworkNotAvailable();
			}
			else
			{
				THLLog.d("debug", "NI not null");

				NetworkInfo niMobile= cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if(null != niMobile)
				{
					boolean is3g	= niMobile.isConnectedOrConnecting();

					if(is3g)
					{
						dlgNetwork3G();
					}
					else
					{
						USBeaconServerInfo info= new USBeaconServerInfo();

						info.serverUrl		= HTTP_API;
						info.queryUuid		= QUERY_UUID;
						info.downloadPath	= STORE_PATH;

					}
				}
			}
		}
		else
		{
			THLLog.d("debug", "CM null");
		}

		mHandler.sendEmptyMessageDelayed(MSG_UPDATE_BEACON_LIST, 500);


		wv = (App2WebView) findViewById(R.id.webView1);
		vv = (VideoView) findViewById(R.id.videoView1);
		wv.setVisibility(WebView.INVISIBLE);
		wv.setWebChromeClient(new WebChromeClient());
		probar = (ProgressBar) findViewById(R.id.progressBar1);
		im = (ImageView) findViewById(R.id.imageView1);
		L1 = (RelativeLayout) findViewById(R.id.LinearLayout01);

		wv.getSettings().setUserAgentString("Mozilla/5.0 (X11;Linux86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24");
		wv.getSettings().setLoadWithOverviewMode(true);// loads the WebView													                                                	// completely zoomed out
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setUseWideViewPort(true);
		//wv.getSettings().setPluginState(PluginState.ON);
		wv.getSettings().setSupportZoom(true);
		wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		wv.addJavascriptInterface(new js2app(), "Web2App");

		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});


		loadWvUrlFunc(_DEFINE_WEBAPP_URL);

		vv.setVisibility(View.GONE);
		//decideConnect.interrupt();
		probar.setVisibility(View.GONE);
		im.setVisibility(View.GONE);
		wv.setVisibility(WebView.VISIBLE);
		//wv.setVisibility(WebView.INVISIBLE);
		Log.d("doInBackground(view)", "vv probar im GONE");
		Log.d("doInBackground(view)", "wv VISIBLE");
		Log.d("doInBackground(view)", "wv VISIBLE");
		//re_entry = true;

		//GGPPSS
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// Getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// Getting network status
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isGPSEnabled && !isNetworkEnabled) {
			// No network provider is enabled
			Log.d("doInBackground", "No network provider is enabled");
		} else {
			this.canGetLocation = true;
			Log.d("doInBackground", "canGetLocation true");
			if (isNetworkEnabled) {
				/*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}*/
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						3000,//MIN_TIME_BW_UPDATES
						0, (LocationListener) this);//MIN_DISTANCE_CHANGE_FOR_UPDATES
				Log.d("Network", "Network");
				if (locationManager != null) {
					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
					}
				}
			}
			// If GPS enabled, get latitude/longitude using GPS Services
			if (isGPSEnabled) {
				if (location == null) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							3000,
							0, (LocationListener) this);
					Log.d("GPS Enabled", "GPS Enabled");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					if (wv.canGoBack()) {
						wv.goBack();
					} else {
						finish();
					}
					return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			//this.updateToNewLocation(location);
			//call_web_Gmap(location);
			double latitude = (double) (location.getLatitude());
			double longitude = (double) (location.getLongitude());

			Log.v("doInBackground", "Geo_Location Latitude: " + latitude + ", Longitude: " + longitude);

			JSONObject j = new JSONObject();
			try {
				j.put("lat", latitude);
				j.put("lng", longitude);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//wv.loadUrl("javascript:alert('" + j + "')");
			callWvJSFunc("calculateAndDisplayRouteInInner('" + j + "')");
			//Toast.makeText(MainActivity.this, "wv GONE", Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	private void call_get_GPS() {

		//GPS_Location gps;
		//gps = new GPS_Location(MainActivity.this);

		// Check if GPS enabled
		if(canGetLocation) {

			double latitude = location.getLatitude();
			double longitude = location.getLongitude();

			// \n is for new line
			//Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

			JSONObject j = new JSONObject();
			try {
				j.put("lat", latitude);
				j.put("lng", longitude);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d("doInBackground", "initMap_cb  " + j);
			callWvJSFunc("javascript:initMap_cb('" + j + "')");

		} else {
			// Can't get location.
			// GPS or network is not enabled.
			// Ask user to enable GPS/network in settings.
		}

	}


	public void loadWvUrlFunc(final String data){
		wv.post(new Runnable() {
			@Override
			public void run() {
				wv.loadUrl(data);
			}
		});
	}

	public void callWvJSFunc(final String data){
		wv.post(new Runnable() {
			@Override
			public void run() {
				wv.loadUrl("javascript:" + data);
			}
		});
	}


	public void connectSuccess() {

		// network connection success.
		if (re_entry == false) {

			if (JudgePortraitLandscape.compareTo("portrait") == 0) {

			} else {

			}

			//onPageFinished();
		}

	}// connectSuccess ended.


	public class js2app {
		@JavascriptInterface
		public void call_get_GPS_Location(){
			Log.v("doInBackground", "GPS init");
			call_get_GPS();
		}

	}
	// ======================  Send to SQL -- end  ============================= //

	/** ================================================ */
	String TAG = "UIMain";
	@Override
	public void onResume()
	{
		Log.d(TAG,"onResume");
		super.onResume();
		Log.d(TAG,"onResume");
	}

	/** ================================================ */
	@Override
	public void onPause()
	{
		super.onPause();
	}

	/** ================================================ */
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}

	/** ================================================ */
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		THLLog.d("DEBUG", "onActivityResult()");

		switch(requestCode)
		{
			case REQ_ENABLE_BT:
				if(RESULT_OK == resultCode)
				{
				}
				break;

			case REQ_ENABLE_WIFI:
				if(RESULT_OK == resultCode)
				{
				}
				break;
		}
	}

	/** ================================================ */
	@Override
	public void onScaned(iBeaconData iBeacon)
	{
	}
	/** ================================================ */
	@Override
	public void onBatteryPowerScaned(BatteryPowerData batteryPowerData) {
		// TODO Auto-generated method stub
		Log.d("debug", batteryPowerData.batteryPower+"");
		for(int i = 0 ; i < miBeacons.size() ; i++)
		{
			if(miBeacons.get(i).macAddress.equals(batteryPowerData.macAddress))
			{
				ScanediBeacon ib = miBeacons.get(i);
				ib.batteryPower = batteryPowerData.batteryPower;
				miBeacons.set(i, ib);
			}
		}
	}

	/** ========================================================== */
	public void onResponse(int msg)
	{
		THLLog.d("debug", "Response("+ msg+ ")");
		mHandler.obtainMessage(MSG_SERVER_RESPONSE, msg, 0).sendToTarget();
	}

	/** ========================================================== */
	public void dlgNetworkNotAvailable()
	{
		final AlertDialog dlg = new AlertDialog.Builder(UIMain.this).create();

		dlg.setTitle("Network");
		dlg.setMessage("Please enable your network for updating beacon list.");

		dlg.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dlg.dismiss();
			}
		});

		dlg.show();
	}

	/** ========================================================== */
	public void dlgNetwork3G()
	{
		final AlertDialog dlg = new AlertDialog.Builder(UIMain.this).create();

		dlg.setTitle("3G");
		dlg.setMessage("App will send/recv data via 3G, this may result in significant data charges.");

		dlg.setButton(AlertDialog.BUTTON_POSITIVE, "Allow", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				Config.allow3G= true;
				dlg.dismiss();
				USBeaconServerInfo info= new USBeaconServerInfo();

				info.serverUrl		= HTTP_API;
				info.queryUuid		= QUERY_UUID;
				info.downloadPath	= STORE_PATH;

			}
		});

		dlg.setButton(AlertDialog.BUTTON_NEGATIVE, "Reject", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				Config.allow3G= false;
				dlg.dismiss();
			}
		});

		dlg.show();
	}

	/** ========================================================== */
	public void verifyiBeacons()
	{
	}

	// fast way to call Toast
	private static void makeTextAndShow(final Context context, final String text, final int duration) {

		if (toast == null) {
			//如果還沒有建立過Toast，才建立
			final ViewGroup toastView = new FrameLayout(context); // 用來裝toastText的容器
			final FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			final GradientDrawable background = new GradientDrawable();
			toastText = new TextView(context);
			toastText.setLayoutParams(flp);
			toastText.setSingleLine(false);
			toastText.setTextSize(30);
			toastText.setTextColor(Color.argb(0xAA, 0xFF, 0xFF, 0xFF)); // 設定文字顏色為有點透明的白色
			background.setColor(Color.argb(0xAA, 0xFF, 0x00, 0x00)); // 設定氣泡訊息顏色為有點透明的紅色
			background.setCornerRadius(20); // 設定氣泡訊息的圓角程度

			toastView.setPadding(30, 30, 30, 30); // 設定文字和邊界的距離
			toastView.addView(toastText);
			toastView.setBackground(background);

			toast = new Toast(context);
			toast.setView(toastView);
		}
		toastText.setText(text);
		toast.setDuration(duration);
		toast.show();
	}


	private static class Data implements Comparable<Data> {

		private double rsi;
		private LocTag locTag;
		//private int counter;

		public Data(double rsi,LocTag locTag) {
			this.rsi = rsi;
			this.locTag = locTag;

		}

		public double getRsi() {
			return rsi;
		}

		public LocTag getLocTag() {
			return locTag;
		}
		public enum LocTag {
			z0_0, z0_1, z0_2, z0_3, z0_4,
			z1_0, z1_1, z1_2, z1_3, z1_4, z1_5,
			z2_0, z2_1, z2_2, z2_3, z2_4, z2_5,
			z3_0, z3_1, z3_2, z3_5,
			z4_0, z4_1,
			z5_0, z5_1, z5_2, z5_3,
			z6_0, z6_1, z6_2, z6_3,
			z7_0, z7_1, z7_2, z7_3, z7_4, z7_5,
			z8_0, z8_1, z8_3, z8_4, z8_5,
			z9_0, z9_1, z9_2, z9_3, z9_4,
			z10_0, z10_1, z10_2, z10_3, z10_4,
			z11_0, z11_1, z11_2, z11_3, z11_4,
			z12_0, z12_1, z12_3, z12_4,
			z13_0, z13_1, z13_2, z13_3, z13_4, z13_5,
			z14_0, z14_1, z14_2, z14_3, z14_4, z14_5,
			z15_0, z15_1, z15_2, z15_3, z15_4, z15_5,
			z16_0, z16_1, z16_2, z16_3, z16_4, z16_5,
			z17_0, z17_1, z17_2, z17_3, z17_4, z17_5,
			z18_0, z18_1, z18_2, z18_3, z18_4, z18_5,
			z19_0, z19_1, z19_2, z19_3, z19_4,
			z20_0, z20_1, z20_2, z20_3, z20_4,
			z21_0, z21_1,
			z22_0, z22_1,
			z23_0, z23_1,
			z24_0, z24_1,
		}
		@Override
		public int compareTo(Data data) {
			// TODO Auto-generated method stub
			//System.out.println("a:"+rsi);
			//System.out.println("b:"+data.rsi);
			if(rsi>data.rsi) return 1;
			else if(rsi<data.rsi) return -1;
			return 0;
		}

	}

	public ArrayList<String> myList = new ArrayList<String>();
	public String[][] trans_array = new String[rawDataNum][5];

}
