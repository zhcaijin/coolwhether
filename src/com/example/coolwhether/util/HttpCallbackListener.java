package com.example.coolwhether.util;

public interface HttpCallbackListener {
	
	void onFinish(String response);
	
	void onError(Exception e);

}
