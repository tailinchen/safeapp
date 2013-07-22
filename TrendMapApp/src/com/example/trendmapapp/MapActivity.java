package com.example.trendmapapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends Activity {
    private GoogleMap map;
    Marker park[]= new Marker[164];
    private Marker markerMe;
    double lng;
    double lat;
    private Marker []allMark = new Marker[62];
    private String[] allpoint = new String[62];
    private String[] allrule = new String[62];
    private String pointData = null;
    String ruleData = null;
    String []passRule = new String[62];
//    private List<String> _points = new ArrayList<String>();
    private String []wholedanger = new String[62];
    private Button safeBtn;
    List<String> all_points = new ArrayList<String>();
    List<String> safe_points = new ArrayList<String>();
    List<String> safe_points2 = new ArrayList<String>();
    Polyline polyline;
    Polyline polylinesafe;
    private String attr[]={"大貨車禁行區","公車站牌","加油站","停車場","停車位個數","警察局","禁停紅黃線區域","即時車速","公廁","觀光景點","wifi熱點"};
    
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
    	  lng = location.getLongitude();
    	  //緯度
    	  lat = location.getLatitude();
    	  //速度
    	  float speed = location.getSpeed();
    	  //時間
    	  long time = location.getTime();

    	  //標出"我"的位置
    	  showMarkerMe(lat, lng);
    	  cameraFocusOnMe(lat, lng);

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
//    	      		Toast.makeText(MapActivity.this, "Status Changed: Out of Service", Toast.LENGTH_SHORT).show();
    		 		break;
    		 	case LocationProvider.TEMPORARILY_UNAVAILABLE:
//    	      		Toast.makeText(MapActivity.this, "Status Changed: Temporarily Unavailable", Toast.LENGTH_SHORT).show();
    		 		break;
    		 	case LocationProvider.AVAILABLE:
//    	      		Toast.makeText(MapActivity.this, "Status Changed: Available", Toast.LENGTH_SHORT).show();
    		 		break;
    		 }
    	 }
    };
    
    private void showMarkerMe(double lat, double lng){
    	 if (markerMe != null) {	//當自己的位置改變時,把之前的標誌拿掉,換新位置
    		 markerMe.remove();
    	 }

    	 MarkerOptions markerOpt = new MarkerOptions();
    	 BitmapDescriptor bitmapDescriptor 
    	   = BitmapDescriptorFactory.defaultMarker(
    	     BitmapDescriptorFactory.HUE_AZURE);
    	 markerOpt.position(new LatLng(lat, lng));
    	 markerOpt.title("目前位置");
    	 markerOpt.icon(bitmapDescriptor);
    	 markerMe = map.addMarker(markerOpt);

    	 Toast.makeText(this, "lat:" + lat + ",lng:" + lng, Toast.LENGTH_SHORT).show();
    }
    
    
    private void cameraFocusOnMe(double lat, double lng){
    	 CameraPosition camPosition = new CameraPosition.Builder()	//把畫面位置移到自己目前的位置
    	    .target(new LatLng(lat, lng))
    	    .zoom(13)
    	    .build();

    	 map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }
    //gps監聽器,觀看目前gps的狀況
    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                Toast.makeText(MapActivity.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
                break;
                case GpsStatus.GPS_EVENT_STOPPED:
                Toast.makeText(MapActivity.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
                break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                Toast.makeText(MapActivity.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_map);
        //使用網路需要使用另外的執行續
		new AsyncTask<Void,Void,Void>(){
			String back = null;
			Bundle bun =  MapActivity.this.getIntent().getExtras();

			@Override
			protected Void doInBackground(Void... arg0) {
				String url = "http://140.116.96.88/trend1.php";
				try{
					HttpClient httpclient = new DefaultHttpClient();
			    	 
			         try {
			             HttpPost httpPost = new HttpPost(url);
			  
			             System.out.println("executing request " + httpPost.getRequestLine());
			             HttpResponse response = httpclient.execute(httpPost);
			             HttpEntity resEntity = response.getEntity();
			  
			             System.out.println("----------------------------------------");
			             System.out.println(response.getStatusLine());
			             if (resEntity != null) {
			                 pointData = EntityUtils.toString(resEntity,"big5"); //這裡要加上編碼
			                 Log.e("testpoint", pointData);
			             }
			             
			         } 
			         catch (Exception e) {
			             System.out.println(e);
			         }
			         
			         url = "http://maps.google.com/maps/api/directions/json?origin="+bun.getString("myloc")+"&destination="+bun.getString("desloc")+"&sensor=false";
			         GetDirection(url,all_points);
			         
			         url = "http://140.116.96.88/trend2.php";
			         try {
			             HttpPost httpPost = new HttpPost(url);
			  
			             System.out.println("executing request " + httpPost.getRequestLine());
			             HttpResponse response = httpclient.execute(httpPost);
			             HttpEntity resEntity = response.getEntity();
			  
			             System.out.println("----------------------------------------");
			             System.out.println(response.getStatusLine());
			             if (resEntity != null) {
			                 ruleData = EntityUtils.toString(resEntity,"big5"); //這裡要加上編碼
			                 Log.e("test", ruleData);
			             }
			             
			         } 
			         catch (Exception e) {
			             System.out.println(e);
			         }
					
				}catch(Exception e){
					e.printStackTrace();
				}
				return null;
			}
			
			protected void onPostExecute(Void result){
				allpoint = pointData.split("#");
				allrule = ruleData.split("#");
//				
				for(int i=0;i<62;i++){
					String temp[];
					String rules[];
					String markrule = "rule";
					
					temp = allpoint[i].split(",");
					rules = temp[2].split(" ");
					
					for(int j=0;j<rules.length;j++){
						String tt = allrule[Integer.parseInt(rules[j])];
						String tempRule[];
						
						tempRule = tt.split(",");
						if(tempRule[2].equals("=")){
							markrule = markrule+(j+1)+":"+attr[Integer.parseInt(tempRule[1])-1]+"="+tempRule[3]+"\n";
						}else if(tempRule[2].equals("<")){
							markrule = markrule+(j+1)+":"+attr[Integer.parseInt(tempRule[1])-1]+"<"+tempRule[3]+"\n";
						}else{
							markrule = markrule+(j+1)+":"+attr[Integer.parseInt(tempRule[1])-1]+">="+tempRule[3]+"\n";
						}
					}
					
					allMark[i] = map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(temp[1]), Double.parseDouble(temp[0])))
						.title("車禍可能規則:")
						.snippet(markrule));
					passRule[i] = markrule;
					wholedanger[i] = temp[1]+","+temp[0];
				}
//				drawPolyline();
				drawPolyline(all_points);
				
				super.onPostExecute(result);
			}
			
		}.execute();
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
        map.setOnInfoWindowClickListener(mapListener);
        safeBtn = (Button)findViewById(R.id.button1);
        safeBtn.setOnClickListener(btnListen);
        
//        map.setInfoWindowAdapter(new InfoWindowAdapter() {
//        	 
//            // Use default InfoWindow frame
//            @Override
//            public View getInfoWindow(Marker arg0) {
//                return null;
//            }
// 
//            // Defines the contents of the InfoWindow
//            @Override
//            public View getInfoContents(Marker arg0) {
// 
//                // Getting view from the layout file info_window_layout
//                View v = getLayoutInflater().inflate(R.layout.window, null);
//                TextView tv = (TextView)findViewById(R.id.textView1);
// 
//                // Getting the position from the marker
//                LatLng latLng = arg0.getPosition();
//                Log.e("ln", latLng.latitude+" "+latLng.longitude);
////                allpoint = pointData.split("#");
////				allrule = ruleData.split("#");
////				
//				int markNum=0;
//            	for(int i=0;i<62;i++){
//            		String temp[];
//					temp = allpoint[markNum].split(",");
//					
//            		if(Double.toString(latLng.latitude).equals(temp[1]) && Double.toString(latLng.longitude).equals(Double.parseDouble(temp[0]))){
//            			markNum = i;
//            			Log.e("test","t"+i);
//            			break;
//            		}
//            	}
////				
////				for(int i=0;i<62;i++){
//					String temp[];
//					String rules[];
//					String markrule = "rule";
//					
//					temp = allpoint[markNum].split(",");
//					rules = temp[2].split(" ");
//					
//					for(int j=0;j<rules.length;j++){
//						String tt = allrule[Integer.parseInt(rules[j])];
//						String tempRule[];
//						
//						tempRule = tt.split(",");
//						if(tempRule[2].equals("=")){
//							markrule = markrule+(j+1)+":"+attr[Integer.parseInt(tempRule[1])-1]+"="+tempRule[3]+"\n";
//						}else if(tempRule[2].equals("<")){
//							markrule = markrule+(j+1)+":"+attr[Integer.parseInt(tempRule[1])-1]+"<"+tempRule[3]+"\n";
//						}else{
//							markrule = markrule+(j+1)+":"+attr[Integer.parseInt(tempRule[1])-1]+">="+tempRule[3]+"\n";
//						}
////					}
//						tv.setText(markrule);
////					allMark[i] = map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(temp[1]), Double.parseDouble(temp[0])))
////							.title("車禍可能規則:")
////							.snippet(markrule));
//				}
// 
//                // Returning the view containing InfoWindow contents
//                return v;
// 
//            }
//        });
    }
    
    int btnClick = 0;
    View.OnClickListener btnListen =  new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			if(btnClick==0){
				polyline.remove();
				Thread t1 = new Thread( new Runnable(){
					@Override
					public void run() {
						redirection();
					}
				});
				t1.start();
				safeBtn.setText("顯示路線");
				btnClick++;
			}else{
				drawPolyline(safe_points);
			}
			
//			drawPolyline(safe_points);
//			safeBtn.setVisibility(View.GONE);
//			safeBtn.setClickable(false);
//			safeBtn.setText("安全路線");
		}
    	
    };
    
//    double alldis = 0;
//    double predis = 0;
    public void redirection(){
    	Bundle bun =  MapActivity.this.getIntent().getExtras();
//    	String t = null;
//    	int fix = judgeSafe(all_points);
//    	for(int i=0;i<3;i++){
//        	String temp[];
//        	temp = all_points.get(fix).split(",");
//        	t = (Double.parseDouble(temp[0])*0.000001+0.05)+","+(Double.parseDouble(temp[1])*0.000001+0.05);
//        	if(i==0){
//        		all_points.add(fix, t);
//        		fix = judgeSafe(all_points);
//        		predis = alldis;
//        	}else if(i!=0 && alldis>predis){
//        		all_points.add(fix, t);
//        		fix = judgeSafe(all_points);
//        		predis = alldis;
//        	}else if(i!=0 && alldis<=predis){
//        		break;
//        	}
//    	}
    	String des = all_points.get(judgeSafe(all_points));
//    	String des = all_points.get(5);
    	String temp[];
    	temp = des.split(",");
    	String t = (Double.parseDouble(temp[0])*0.000001+0.02)+","+(Double.parseDouble(temp[1])*0.000001+0.02);
    	Log.d("safe2", t);
    	
    	String url = "http://maps.google.com/maps/api/directions/json?origin="+bun.getString("myloc")+"&destination="+t+"&sensor=false";
        GetDirection(url,safe_points);

        
        url = "http://maps.google.com/maps/api/directions/json?origin="+t+"&destination="+bun.getString("desloc")+"&sensor=false";
        GetDirection(url,safe_points);
    }
    
    public int judgeSafe(List<String> road){
    	double mindis =0;
    	double allpointDis[] = new double[62]; //
    	double pickpoint[] = new double[road.size()]; //所有點跟危險點前三近的距離加總
    	double alldis = 0;
    	
//    	for(int times = 0;times<3;times++){
    		for(int j=0;j<road.size();j++){ //所有轉折點
    			String temp[];
    			temp = road.get(j).split(","); //轉折點
    			double pointDis[] = new double[road.size()];
    			
    			for(int i=0;i<62;i++){ //所有危險點
    	    		String tempwhole[];
    	    		tempwhole = wholedanger[i].split(","); //第i筆危險
    	    		double dis1 = Double.parseDouble(tempwhole[0])-Double.parseDouble(temp[0]);
    	    		double dis2 = Double.parseDouble(tempwhole[1])-Double.parseDouble(temp[1]);
    	    		//轉折點i跟所有危險點的距離
    	    		pointDis[i] = Math.sqrt(Math.pow(dis1, 2)+ Math.pow(dis2, 2));
    	    		//轉折點i跟所有危險點的距離加總
    	    		alldis += Math.sqrt(Math.pow(dis1, 2)+ Math.pow(dis2, 2));
    			}
    			
    			Arrays.sort(pointDis);
        		for(int t=0;t<road.size();t++){
        			for(int k=0;k<3;k++){
        				pickpoint[t]+=pointDis[k];
        			}
        		}
    		}

    		double min = 10000000;
    		int minindex = 1; //要被去除的點的index直
    		for(int i=1;i<pickpoint.length;i++){
    			if(pickpoint[i]<min){
    				min = pickpoint[i];
    				minindex = i;
    			}
    		}
    		Log.d("mini", Integer.toString(minindex));
//    	}
    		return minindex;
    }
    
    public List<String> GetDirection(String url,List<String> p)
    {
//        String mapAPI = "http://maps.google.com/maps/api/directions/json?origin={0}&destination={1}&language=zh-TW&sensor=true";
//        String url = MessageFormat.format(mapAPI, _from, _to);

        HttpGet get = new HttpGet(url);
        String strResult = "";
        try
        {

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
            HttpClient httpClient = new DefaultHttpClient(httpParameters);

            HttpResponse httpResponse = null;
            httpResponse = httpClient.execute(get);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                strResult = EntityUtils.toString(httpResponse.getEntity());

                JSONObject jsonObject = new JSONObject(strResult);
                JSONArray routeObject = jsonObject.getJSONArray("routes");
                String polyline = routeObject.getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                

                if (polyline.length() > 0)
                {
                    decodePolylines(polyline,p);
                }

            }
        }
        catch (Exception e)
        {
            Log.e("map", "MapRoute:" + e.toString());
        }

        return p;
    }

    private void decodePolylines(String poly,List<String> _points)
    {
        int len = poly.length();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len)
        {
            int b, shift = 0, result = 0;
            do
            {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do
            {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            String p = (int) (((double) lat / 1E5) * 1E6)+","+ (int) (((double) lng / 1E5) * 1E6);
            Log.e("testroute", p);
            _points.add(p);

        }
    }
    
    private void drawPolyline(List<String> _points){
        PolylineOptions polylineOpt = new PolylineOptions();
        for(String t:_points){
        	String temp[];
        	temp=t.split(",");
        	System.out.println(_points);
        	polylineOpt.add(new LatLng(Double.parseDouble(temp[0])*0.000001,Double.parseDouble(temp[1])*0.000001));
        }

        polylineOpt.color(Color.BLUE);

//        Polyline polyline = map.addPolyline(polylineOpt);
        polyline = map.addPolyline(polylineOpt);
        polyline.setWidth(10);
    }
    
    
    //window listner
    GoogleMap.OnInfoWindowClickListener mapListener =  new GoogleMap.OnInfoWindowClickListener(){
		@Override
		public void onInfoWindowClick(Marker arg0) {	//當有人按下標誌上的window時會觸發這個事件
			int index=0;
			Bundle b = new Bundle();
			for(int i=0;i<allMark.length;i++){
				if(arg0 == allMark[i]){
					index = i;
				}
			}
			Intent intent = new Intent();
			b.putString("search", passRule[index] );
			intent.putExtras(b);
			intent.setClass(MapActivity.this, WindowActivity.class);
			startActivity(intent);
		}
    };
}
