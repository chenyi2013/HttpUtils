package com.example.httpdemo;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 本类主要封装了从网络上加载数据和上传文件到网络服务器等操作
 * 
 * @author Kevin
 * 
 */
public class HttpUtils {

	public static final int GET = 1;
	public static final int POST = 2;

	private static final int LOAD_DATA_SUCCES = 1;
	private static final int LOAD_DATA_ERROR = 2;
	private static final String STARTBOUNDARY = "---------------------------7de11e16190796";
	private static final String BOUNDARY = "--" + STARTBOUNDARY + "\r\n";
	private static final String ENDBOUNDARY = "\r\n--" + STARTBOUNDARY
			+ "--\r\n";

	public interface Response {

		public void getData(byte[] data);

	}

	/**
	 * 以GET形式加载网络数据
	 * 
	 * @param url
	 *            所要加载数据的url
	 * @param callback
	 *            用于传递数据的回调接口
	 */
	public static void loadNetworkData(String url, Response callback) {

		loadNetworkData(GET, url, null, null, callback);

	}

	public static void loadNetworkData(String url,
			HashMap<String, String> headers, Response callback) {

		loadNetworkData(GET, url, headers, null, callback);

	}

	public static void loadNetworkData(int method, String url, Response callback) {

		loadNetworkData(method, url, null, null, callback);

	}

	public static void loadNetworkData(int method, String url,
			HashMap<String, String> headers, Response callback) {

		loadNetworkData(method, url, headers, null, callback);

	}

	public static void loadNetworkData(final int method, final String url,
			final HashMap<String, String> headers, final byte[] body,
			final Response callback) {

		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case LOAD_DATA_SUCCES:
					callback.getData((byte[]) msg.obj);

					break;
				case LOAD_DATA_ERROR:
					callback.getData(null);
					break;
				}

			}
		};

		new Thread(new Runnable() {

			@Override
			public void run() {

				byte[] data = getData(method, headers, body, url);
				Message message = handler.obtainMessage();

				if (data != null) {

					message.what = LOAD_DATA_SUCCES;
					message.obj = data;
				} else {
					message.what = LOAD_DATA_ERROR;
				}

				message.sendToTarget();

			}
		}).start();
	}

	public static byte[] getData(int method, HashMap<String, String> propertys,
			byte[] body, String path) {

		try {

			URL url = new URL(path);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.setUseCaches(false);

			if (propertys != null) {
				for (String headerName : propertys.keySet()) {
					connection.addRequestProperty(headerName,
							propertys.get(headerName));
				}
			}

			switch (method) {

			case POST:
				connection.setRequestMethod("POST");
				if (body != null) {

					connection.setDoOutput(true);
					DataOutputStream outputStream = new DataOutputStream(
							connection.getOutputStream());
					outputStream.write(body);
					outputStream.close();

				}
				break;

			case GET:
				connection.setRequestMethod("GET");
				break;

			default:
				throw new IllegalStateException("Unknown method type.");

			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			InputStream inputStream = connection.getInputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}

			if (connection.getResponseCode() == 200) {
				return outputStream.toByteArray();
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 上传文件
	 * 
	 * @param path
	 * @param fileList
	 */
	public static void uploadFile(final String path,
			final ArrayList<String> fileList) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Log.i("kevin", "start");
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setUseCaches(false);
					conn.setRequestMethod("POST");
					conn.addRequestProperty("connection", "Keep-Alive");
					conn.addRequestProperty("user-agent",
							"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
					conn.addRequestProperty("Charsert", "UTF-8");
					conn.addRequestProperty("Content-Type",
							"multipart/form-data; boundary=" + BOUNDARY);

					OutputStream out = new DataOutputStream(conn
							.getOutputStream());
					StringBuffer sb = null;
					Log.i("kevin", "start1");
					for (int i = 0; i < fileList.size(); i++) {
						Log.i("kevin", "start2");
						sb = new StringBuffer();
						sb.append(BOUNDARY);
						sb.append("Content-Disposition: form-data; name=\"file"
								+ i + "\"; filename=\"" + fileList.get(i)
								+ "+\"\r\n");
						sb.append("Content-Type: application/octet-stream\r\n\r\n");
						out.write(sb.toString().getBytes());
						DataInputStream inputStream = new DataInputStream(
								new FileInputStream(new File(fileList.get(i))));
						int bytes = 0;
						byte buffer[] = new byte[1024];
						while ((bytes = inputStream.read(buffer)) != -1) {

							out.write(buffer, 0, bytes);
							Log.i("kevin", "start3");
						}
						out.write("\r\n".getBytes());
						inputStream.close();
					}

					out.write(ENDBOUNDARY.getBytes());
					out.flush();
					out.close();

					Log.i("kevin", "end");

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();

	}
}
