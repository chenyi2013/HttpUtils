package com.example.httpdemo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final TextView tv = (TextView) findViewById(R.id.text_view);
		System.out.println("start");
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// upload();
		//
		// }
		// }).start();

		File Dir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		String fileName = Dir.getAbsolutePath() + File.separator + "ab.jpg";
		ArrayList<String> files = new ArrayList<String>();
		files.add(fileName);
		HttpUtils.uploadFile("http://192.168.1.58/PhpProject1/doAction.php",
				files);

	}

	public void upload() {
		List<String> list = new ArrayList<String>(); // Ҫ�ϴ����ļ���,�磺d:\haha.doc.��Ҫʵ���Լ���ҵ�����������һ����list.
		File Dir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		String fileName = Dir.getAbsolutePath() + File.separator + "ab.jpg";
		list.add(fileName);
		try {
			String BOUNDARY = "---------7d4a6d158c9"; // �������ݷָ���
			URL url = new URL("http://192.168.1.58/PhpProject1/doAction.php");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// ����POST�������������������
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Charsert", "UTF-8");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(conn.getOutputStream());
			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// ����������ݷָ���
			int leng = list.size();
			for (int i = 0; i < leng; i++) {
				String fname = list.get(i);
				File file = new File(fname);
				StringBuilder sb = new StringBuilder();
				sb.append("--");
				sb.append(BOUNDARY);
				sb.append("\r\n");
				sb.append("Content-Disposition: form-data;name=\"file" + i
						+ "\";filename=\"" + file.getName() + "\"\r\n");
				sb.append("Content-Type:application/octet-stream\r\n\r\n");

				byte[] data = sb.toString().getBytes();
				out.write(data);
				DataInputStream in = new DataInputStream(new FileInputStream(
						file));
				int bytes = 0;
				byte[] bufferOut = new byte[1024];
				while ((bytes = in.read(bufferOut)) != -1) {
					out.write(bufferOut, 0, bytes);
				}
				out.write("\r\n".getBytes()); // ����ļ�ʱ�������ļ�֮��������
				in.close();
			}
			out.write(end_data);
			out.flush();
			out.close();

			// ����BufferedReader����������ȡURL����Ӧ
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

		} catch (Exception e) {
			System.out.println("����POST��������쳣��" + e);
			e.printStackTrace();
		}
	}
}
