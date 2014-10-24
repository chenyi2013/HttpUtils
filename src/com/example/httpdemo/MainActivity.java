package com.example.httpdemo;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// File Dir = Environment
				// .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				// String fileName = Dir.getAbsolutePath() + File.separator
				// + "ab.jpg";
				// ArrayList<String> files = new ArrayList<String>();
				// files.add(fileName);
				// HttpUtils.useHttpMimeUploadFile(
				// "http://192.168.1.58/PhpProject1/a.php", files);
				// HttpUtils.loadNetworkData(HttpUtils.POST,
				// "http://192.168.1.58/PhpProject1/a.php", null,
				// "username=chenyi&& sex=haha".getBytes(),
				// new Response() {
				//
				// @Override
				// public void getData(byte[] data) {
				//
				// System.out.println(new String(data));
				//
				// }
				// });

				new Thread(new Runnable() {

					@Override
					public void run() {
						HashMap<String, String> forms = new HashMap<String, String>();
						forms.put("username", "eileen");
						forms.put("sex", "man");
						System.out.println(new String(
								HttpUtils
										.useHttpClientGetData(HttpUtils.POST,
												null, forms,
												"http://192.168.1.58/PhpProject1/a.php")));

					}
				}).start();

			}
		});

	}

}
