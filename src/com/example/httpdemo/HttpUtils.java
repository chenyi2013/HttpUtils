package com.example.httpdemo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;

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
	private static final String STARTBOUNDARY = "----WebKitFormBoundaryiAAgDoILaQMj1Ee2";
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

	public static byte[] useHttpClientGetData(int method,
			HashMap<String, String> propertys, byte[] body, String path) {

		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = null;

		switch (method) {
		case POST:
			HttpPost httpPost = new HttpPost(path);
			for (String headerName : propertys.keySet()) {
				httpPost.addHeader(headerName, propertys.get(headerName));
			}

			if (body != null) {
				ByteArrayEntity entity = new ByteArrayEntity(body);
				httpPost.setEntity(entity);
			}

			try {
				httpResponse = httpClient.execute(httpPost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;

		case GET:
			HttpGet httpGet = new HttpGet(path);
			for (String headerName : propertys.keySet()) {
				httpGet.addHeader(headerName, propertys.get(headerName));
			}

			try {
				httpResponse = httpClient.execute(httpGet);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;

		default:
			throw new IllegalStateException("Unknown method type.");

		}

		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

			try {
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					return EntityUtils.toByteArray(httpResponse.getEntity());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 向服务器端传递表单数据并返回结果
	 * 
	 * @param method
	 * @param header
	 * @param forms
	 * @param path
	 * @return
	 */
	public static byte[] getData(int method, HashMap<String, String> header,
			HashMap<String, String> forms, String path) {

		StringBuilder sb = new StringBuilder();
		if (forms != null) {

			for (String name : forms.keySet()) {

				sb.append(name);
				sb.append("=");
				sb.append(forms.get(name));
				sb.append("&");
			}
			sb.deleteCharAt(sb.length() - 1);

		}

		return getData(method, header, sb.toString().getBytes(), path);
	}

	public static byte[] useHttpClientGetData(int method,
			HashMap<String, String> propertys, HashMap<String, String> forms,
			String path) {

		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = null;

		switch (method) {
		case POST:

			HttpPost post = new HttpPost(path);
			if (propertys != null) {
				for (String headerName : propertys.keySet()) {
					post.setHeader(headerName, propertys.get(headerName));
				}
			}

			if (forms != null) {

				ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
				for (String name : forms.keySet()) {

					pairs.add(new BasicNameValuePair(name, forms.get(name)));
				}
				try {
					UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(
							pairs);
					post.setEntity(encodedFormEntity);
					httpResponse = httpClient.execute(post);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;

		case GET:
			HttpGet get = new HttpGet(path);
			if (propertys != null) {
				for (String headerName : propertys.keySet()) {
					get.setHeader(headerName, propertys.get(headerName));
				}
			}

			try {
				httpResponse = httpClient.execute(get);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;

		default:
			throw new IllegalStateException("Unknown method type.");

		}

		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			try {
				return EntityUtils.toByteArray(httpResponse.getEntity());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

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
	 * @param url
	 * @param files
	 */
	public static void useHttpMimeUploadFile(final String url,
			final ArrayList<String> files) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				MultipartEntityBuilder build = MultipartEntityBuilder.create();

				for (String file : files) {

					FileBody bin = new FileBody(new File(file));
					StringBody comment = new StringBody("commit",
							ContentType.TEXT_PLAIN);
					build.addPart(file, bin);
					build.addPart("comment", comment);

				}

				HttpEntity reqEntity = build.build();
				httpPost.setEntity(reqEntity);

				try {
					HttpResponse httpResponse = httpClient.execute(httpPost);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						HttpEntity resEntity = httpResponse.getEntity();
						System.out.println(EntityUtils.toString(resEntity));
					} else {
						System.out.println("connection error!!!!");
					}

				} catch (ClientProtocolException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				} finally {
					httpClient.getConnectionManager().shutdown();
				}
			}
		}).start();

	}

	/**
	 * 上传文件
	 * 
	 * @param path
	 * @param fileList
	 */
	public static void useHttpURLConnectionUploadFile(final String path,
			final ArrayList<String> fileList) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();

					conn.setRequestMethod("POST");
					conn.setRequestProperty("Connection", "keep-alive");
					conn.setRequestProperty("Cache-Control", "max-age=0");
					conn.setRequestProperty("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					conn.setRequestProperty(
							"user-agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari");
					conn.setRequestProperty("Accept-Encoding",
							"gzip,deflate,sdch");
					conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
					conn.setRequestProperty("Charsert", "UTF-8");
					conn.setRequestProperty("Content-Type",
							"multipart/form-data; boundary=" + STARTBOUNDARY);

					OutputStream out = new DataOutputStream(conn
							.getOutputStream());
					StringBuffer sb = null;
					for (int i = 0; i < fileList.size(); i++) {
						File file = new File(fileList.get(i));
						sb = new StringBuffer();
						sb.append(BOUNDARY);
						sb.append("Content-Disposition: form-data; name=\"file"
								+ i + "\"; filename=\"" + file.getName()
								+ "\"\r\n");
						sb.append("Content-Type: application/octet-stream\r\n\r\n");
						out.write(sb.toString().getBytes());

						DataInputStream inputStream = new DataInputStream(
								new FileInputStream(new File(fileList.get(i))));

						int bytes = 0;
						byte buffer[] = new byte[1024];
						while ((bytes = inputStream.read(buffer)) != -1) {

							out.write(buffer, 0, bytes);
						}
						out.write("\r\n".getBytes());
						inputStream.close();
					}

					out.write(ENDBOUNDARY.getBytes());
					out.flush();
					out.close();

					// 定义BufferedReader输入流来读取URL的响应
					// 此处必须得到服务器端的输入流否则上传文件不成功（当php作服务器端语言的时候是这样，其它语言未试)
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					String line = null;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();

	}
}
