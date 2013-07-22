package com.example.trendmapapp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InputActivity extends Activity {
	private Button routeBtn;
	private Button startBtn;
	private EditText inputText;
	String location_string;
	Handler mHandler;
	Double mylng;
	Double mylat;
    /** GPS */
    private LocationManager locationMgr;
    private String provider;
	
	 @Override
	    protected void onStart() {
	        super.onStart();

	        if (initLocationProvider()) {
	            whereAmI();
	        }else{
	        }
	    }

	    @Override
	    protected void onStop() {
	        locationMgr.removeUpdates(locationListener);
	        super.onStop();
	    }
	    
	  //初始化gps服務
	    private boolean initLocationProvider() {
	    	 locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    	 if (locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	    		  provider = LocationManager.GPS_PROVIDER;
	    		  return true;
	    	 }
	    	 return false;
	    }
	    
	    private void updateWithNewLocation(Location location) {
	    	 if (location != null) {
	    	  //經度
	    	  mylng = location.getLongitude();
	    	  //緯度
	    	  mylat = location.getLatitude();
	    	  //速度
	    	  float speed = location.getSpeed();
	    	  //時間
	    	  long time = location.getTime();
	    	  Toast.makeText(InputActivity.this, mylng+","+mylat, Toast.LENGTH_SHORT).show();
	    	 }
	    }
	    
	    //位置監聽器,監看位置改變
	    LocationListener locationListener = new LocationListener(){
	    	 @Override
	    	 public void onLocationChanged(Location location) {
	    	  updateWithNewLocation(location);
	    	 }

	    	 @Override
	    	 public void onProviderDisabled(String provider) {
	    	  updateWithNewLocation(null);
	    	 }

	    	 @Override
	    	 public void onProviderEnabled(String provider) {

	    	 }

	    	 @Override
	    	 public void onStatusChanged(String provider, int status, Bundle extras) {
	    		 switch (status) {
	    		 	case LocationProvider.OUT_OF_SERVICE:
	    	      		Toast.makeText(InputActivity.this, "Status Changed: Out of Service", Toast.LENGTH_SHORT).show();
	    		 		break;
	    		 	case LocationProvider.TEMPORARILY_UNAVAILABLE:
	    	      		Toast.makeText(InputActivity.this, "Status Changed: Temporarily Unavailable", Toast.LENGTH_SHORT).show();
	    		 		break;
	    		 	case LocationProvider.AVAILABLE:
	    	      		Toast.makeText(InputActivity.this, "Status Changed: Available", Toast.LENGTH_SHORT).show();
	    		 		break;
	    		 }
	    	 }
	    };
	    
	    
	    //gps監聽器,觀看目前gps的狀況
	    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
	        @Override
	        public void onGpsStatusChanged(int event) {
	            switch (event) {
	                case GpsStatus.GPS_EVENT_STARTED:
	                Toast.makeText(InputActivity.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
	                break;
	                case GpsStatus.GPS_EVENT_STOPPED:
	                Toast.makeText(InputActivity.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
	                break;
	                case GpsStatus.GPS_EVENT_FIRST_FIX:
	                Toast.makeText(InputActivity.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
	                break;
	                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	                break;
	           }
	        }
	    };
	    
	    private void whereAmI(){
	    	 //取得上次已知的位置
	    	 Location location = locationMgr.getLastKnownLocation(provider);
	    	 updateWithNewLocation(location);

	    	//位置監控器使用的GPS Listener
	    	 locationMgr.addGpsStatusListener(gpsListener);

	    	//Location Listener,當距離超過五公尺時更新,更新時間五秒鐘
	    	 int minTime = 5000;//ms
	    	 int minDist = 5;//meter
	    	 locationMgr.requestLocationUpdates(provider, minTime, minDist, locationListener);
	    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input);
		
		findViews();
		setListeners();
	}
	
	private void findViews(){
		routeBtn = (Button)findViewById(R.id.button1);
		startBtn = (Button)findViewById(R.id.button2);
		inputText = (EditText)findViewById(R.id.editText1);
	}
	
	private void setListeners(){
		routeBtn.setOnClickListener(btnListen);
		startBtn.setOnClickListener(btnListen2);
	}
	
	public JSONObject getLocationInfo() {
		HttpClient httpclient = new DefaultHttpClient();
		String in = inputText.getText().toString();
		String responseBody = null;
		String url = "http://maps.googleapis.com/maps/api/geocode/json?address="+in+"&sensor=true";
		 try {
             HttpPost httpPost = new HttpPost(url);
  
             System.out.println("executing request " + httpPost.getRequestLine());
             HttpResponse response = httpclient.execute(httpPost);
             HttpEntity resEntity = response.getEntity();
  
             System.out.println("----------------------------------------");
             System.out.println(response.getStatusLine());
             if (resEntity != null) {
                 responseBody = EntityUtils.toString(resEntity,"big5"); //這裡要加上編碼
                 Log.e("test", responseBody);
             }
             
         } 
         catch (Exception e) {
             System.out.println(e);
         }
		

        JSONObject jsonObject = new JSONObject();
        try {
//            jsonObject = new JSONObject(stringBuilder.toString());
        	jsonObject = new JSONObject(responseBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
	
	Button.OnClickListener btnListen2 = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			
			if(location_string.length()>0 && location_string!=null){
				bundle.putString("desloc", location_string);
			//	bundle.putString("desloc", "25.063,121,52");
				bundle.putString("myloc", mylat.toString()+","+mylng.toString());
			//	bundle.putString("myloc", "25.062,121.47");
				intent.putExtras(bundle);
				intent.setClass(InputActivity.this, MapActivity.class);
				startActivity(intent);
			}else{
				Toast.makeText(InputActivity.this, "找不到對應地址,請重新輸入" , Toast.LENGTH_SHORT).show();
			}
			
		}
	
	};
	
	private AlertDialog getAlertDialog(String title,String message){
        //產生一個Builder物件
        Builder builder = new AlertDialog.Builder(InputActivity.this);
        //設定Dialog的標題
        builder.setTitle(title);
        //設定Dialog的內容
        builder.setMessage(message);
        //設定Positive按鈕資料
        builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //按下按鈕時顯示快顯
            	
            }
        });
        //利用Builder物件建立AlertDialog
        return builder.create();
    }
	
	int btnClick = 0;
	Button.OnClickListener btnListen = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			if(btnClick==0){
			Thread t1 = new Thread(new Runnable(){

				@Override
				public void run() {
					JSONObject ret = getLocationInfo(); 
					JSONObject location;
					JSONObject location2;
					JSONObject location3;
					String loc;
					String lat;
					String lng;
					try {
					    location = ret.getJSONArray("results").getJSONObject(0);
					    location2 = location.getJSONObject("geometry");
					    location3 = location2.getJSONObject("location");
					    lat = location3.getString("lat");
					    lng = location3.getString("lng");
					    loc = lat+","+lng;
					    location_string = loc;
					    
//					    Toast.makeText(InputActivity.this, "已經取得"+in+"的位置" , Toast.LENGTH_SHORT).show();
					//    Toast.makeText(InputActivity.this, location+location_string , Toast.LENGTH_SHORT).show();
					    Log.d("test", "formattted address:" + location_string);
					} catch (JSONException e1) {
					    e1.printStackTrace();

					}
					
				}
				
			});
			t1.start();
			btnClick++;
			String in = inputText.getText().toString();
//			if(location_string==null || location_string.length()<=0){
//				final AlertDialog alertDialog = getAlertDialog("找不到"+in+"的位置","請重新輸入");
//				alertDialog.show();
//			}else{
//				final AlertDialog alertDialog = getAlertDialog(in+"已訂位完成","請案開始規劃繼續");
//				alertDialog.show();
//			}
			Toast.makeText(InputActivity.this, "已經取得"+in+"的位置" , Toast.LENGTH_SHORT).show();
			routeBtn.setText("開始規劃");
			}else{
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				
				if(location_string.length()>0 && location_string!=null){
					bundle.putString("desloc", location_string);
				//	bundle.putString("desloc", "25.063,121,52");
					bundle.putString("myloc", mylat.toString()+","+mylng.toString());
				//	bundle.putString("myloc", "25.062,121.47");
					intent.putExtras(bundle);
					intent.setClass(InputActivity.this, MapActivity.class);
					startActivity(intent);
				}else{
					Toast.makeText(InputActivity.this, "找不到對應地址,請重新輸入" , Toast.LENGTH_SHORT).show();
				}
			}
			
//			if(location_string.length()>0 && location_string!=null){
////				bundle.putString("search", location_string);
////				intent.putExtras(bundle);
////				intent.setClass(InputActivity.this, MainActivity.class);
////				startActivity(intent);
//			}else{
//				Toast.makeText(InputActivity.this, "找不到對應地址,請重新輸入" , Toast.LENGTH_SHORT).show();
//			}
			
//			String in = inputText.getText().toString();
//			JSONObject ret = getLocationInfo(); 
//			JSONObject location;
//			String location_string;
//			try {
//			    location = ret.getJSONArray("results").getJSONObject(0);
//			    location_string = location.getString("formatted_address");
//			    
//			    Toast.makeText(InputActivity.this, location_string , Toast.LENGTH_SHORT).show();
//			    Log.d("test", "formattted address:" + location_string);
//			} catch (JSONException e1) {
//			    e1.printStackTrace();
//
//			}
//			if(in.length()>0){
//				Geocoder geocoder = new Geocoder(InputActivity.this); 
//				List<Address> addresses = null;
//				Address address = null;
//				try{
//					addresses = geocoder.getFromLocationName("台南市裕信路", 1);
//				}catch(IOException e){
//					Log.e("testinput", e.toString());
//				}
//				
//				if(addresses == null || addresses.isEmpty()){
//					Toast.makeText(InputActivity.this, "找不到該地址", Toast.LENGTH_SHORT).show();
//				}else{
//					address = addresses.get(0);
//					Double lat = address.getLatitude()*1E6;
//					Double lng = address.getLongitude()*1E6;
//					String geo = lat.toString()+","+lng.toString();
//					
//					Toast.makeText(InputActivity.this, geo, Toast.LENGTH_SHORT).show();
//					
////					bundle.putString("search", geo);
////					intent.putExtras(bundle);
////					intent.setClass(InputActivity.this, MapActivity.class);
////					startActivity(intent);
//				}
//			}else{
//				Toast.makeText(InputActivity.this, "找不到該地址", Toast.LENGTH_SHORT).show();
//			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.input, menu);
		return true;
	}

}
