package com.example.httpdemo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final TextView tv = (TextView) findViewById(R.id.text_view);

		String file = "file:///android_asset/ab.jpg";

		HashMap<String, String> headers = new HashMap<>();
		headers.put("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)");
		headers.put("Content-Type", "application/json");
		headers.put("Connection", "Keep-Alive");
		String body = "{\"cmd\":\"GetAddress\",\"deviceid\":\"A41333WE87CF94E39F10301\"}";
		ArrayList<String> files = new ArrayList<String>();
		files.add(file);
		HttpUtils.uploadFile("http://192.168.1.58/PhpProject1/doAction.php",
				files);

	}
}
