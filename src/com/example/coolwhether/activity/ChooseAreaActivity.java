package com.example.coolwhether.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.coolwhether.R;
import com.example.coolwhether.db.CoolWeatherDB;
import com.example.coolwhether.model.City;
import com.example.coolwhether.model.County;
import com.example.coolwhether.model.Province;
import com.example.coolwhether.util.HttpCallbackListener;
import com.example.coolwhether.util.HttpUtil;
import com.example.coolwhether.util.Utility;

import android.app.Activity;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressdialog;
	private TextView titletext;
	private ListView listview;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolweatherdb;
	private List<String> datalist = new ArrayList<String>();
	
	private List<Province> provincelist;
	private List<City> citylist;
	private List<County> countylist;
	
	private Province selectedprovince;
	private City selectedcity;
	//private County selectedcounty;
	private int currentlevel;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listview = (ListView) findViewById(R.id.list_view);
		titletext = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
		listview.setAdapter(adapter);
		coolweatherdb = CoolWeatherDB.getInstance(this);
		listview.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3){
				if (currentlevel == LEVEL_PROVINCE){
					selectedprovince = provincelist.get(index);
					queryCities();
				}else if (currentlevel == LEVEL_CITY){
					selectedcity = citylist.get(index);
					queryCounties();
				}
			}
		});
		queryProvinces();
	}

	private void queryProvinces() {
		// TODO Auto-generated method stub
		provincelist = coolweatherdb.loadProvinces();
		if (provincelist.size() > 0){
			datalist.clear();
			for (Province province : provincelist)
				datalist.add(province.getProvinceName());
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titletext.setText("中国");
			currentlevel = LEVEL_PROVINCE;
		}else {
			queryFromServer(null, "province");
		}
	}
	
	
	private void queryCities() {
		// TODO Auto-generated method stub
		citylist = coolweatherdb.loadCities(selectedprovince.getId());
		if (citylist.size() > 0){
			datalist.clear();
			for (City city : citylist)
				datalist.add(city.getCityName());
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titletext.setText(selectedprovince.getProvinceName());
			currentlevel = LEVEL_CITY;
		}else {
			queryFromServer(selectedprovince.getProvinceCode(), "city");
		}
	}
	
	
	private void queryCounties() {
		// TODO Auto-generated method stub
		countylist = coolweatherdb.loadCounties(selectedcity.getId());
		if (countylist.size() > 0){
			datalist.clear();
			for (County county : countylist)
				datalist.add(county.getCountyName());
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titletext.setText(selectedcity.getCityName());
			currentlevel = LEVEL_COUNTY;
		}else {
			queryFromServer(selectedcity.getCityCode(), "county");
		}
	}
	
	
	private void queryFromServer(final String code, final String type) {
		// TODO Auto-generated method stub
		String address;
		if (!TextUtils.isEmpty(code))
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		else
			address = "http://www.weather.com.cn/data/list3/city.xml";
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if ("province".equals(type))
					result = Utility.handleProvincesResponse(coolweatherdb, response);
				else if ("city".equals(type))
					result = Utility.handleCitiesResponse(coolweatherdb, response, selectedprovince.getId());
				else if ("county".equals(type))
					result = Utility.handleCountiesResponse(coolweatherdb, response, selectedcity.getId());
				if  (result)
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if ("province".equals(type))
								queryProvinces();
							else if ("city".equals(type))
								queryCities();
							else if ("county".equals(type))
								queryCounties();
						}

					});
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if (progressdialog == null){
			progressdialog = new ProgressDialog(this);
			progressdialog.setMessage("加载失败...");
			progressdialog.setCanceledOnTouchOutside(false);
		}
		progressdialog.show();
	}

	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (progressdialog != null)
			progressdialog.dismiss();
	}

	@Override
	public void onBackPressed() {
		if (currentlevel == LEVEL_COUNTY)
			queryCities();
		else if (currentlevel == LEVEL_CITY)
			queryProvinces();
		else 
			finish();
	}
	
}
