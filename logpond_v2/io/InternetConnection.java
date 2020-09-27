package com.infocomm.logpond_v2.io;

import android.content.Context;

import com.infocomm.logpond_v2.storage.SharedPreferencesStorage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class InternetConnection {

	public static final int HTTP = 0;
	public static final int HTTPS = 1;
	public static String URL = "http://infocomm.homelinux.com:8830/Lms";
	//public static final String URL = "http://192.168.1.178";
	private String path;
	private int type;
	private int status;
	private boolean isOpen;
	private HttpURLConnection con;
	private HttpsURLConnection cons;

	public InternetConnection(){
		status = 0;
	}

	/**
	 * Constructor
	 */
	public InternetConnection(String path, int type){
		this.type = type;
		this.path = path;
		status = 0;
		isOpen = true;
	}

	public void connect() throws Exception {
		try{
			switch(type){
				case HTTP:
					con = (HttpURLConnection) new URL(URL + path).openConnection();
					con.setConnectTimeout(10000);
					break;

				case HTTPS:
					cons = (HttpsURLConnection) new URL(URL + path).openConnection();
					cons.setConnectTimeout(10000);
					break;
			}
			isOpen = true;
		}catch(Exception e){
			throw new Exception("Internet Connection Failed");
		}
	}

	public void disconnect() throws Exception {
		try{
			switch(type){
				case HTTP:
					if(con!=null)
						con.disconnect();
					break;

				case HTTPS:
					if(cons!=null)
						cons.disconnect();
					break;
			}
			isOpen = false;
		}catch(Exception e){
			throw new Exception("Internet Connection Failed");
		}
	}

	public boolean isOpen(){
		return isOpen;
	}

	public byte[] sendResponse(Context context, String userData) throws Exception {
		URL = SharedPreferencesStorage.getStringValue(context, SharedPreferencesStorage.SERVER_HOST);
		String errMsg = new String();

		for(int i=0;i<5;i++){
			if(!isOpen) break;
			try{

				switch(type){
					case HTTP:
						con = (HttpURLConnection) new URL(URL + path).openConnection();
						con.setRequestProperty("Content- Type", "application/plain");
						con.setConnectTimeout(10000);
						con.setRequestMethod("GET");
						con.setDoOutput(false);
						con.setDoInput(true);
						con.connect();
						/* Send data */
				    	/*
						OutputStream out =con.getOutputStream();
						out.write(userData.getBytes());
						out.close();
						*/
						/* Receive data */
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						InputStream in = con.getInputStream();
						byte[] bytes = new byte[1024];
						int byteInt;
						while((byteInt = in.read(bytes)) != -1){
							baos.write(bytes, 0, byteInt);
						}
						in.close();
						return baos.toByteArray();


					case HTTPS:

						break;
				}

			}catch(Exception e){
				e.printStackTrace();
				errMsg = e.toString();
				System.out.println("HttpHandler:" + errMsg);
			}finally{
				if(con!=null)
					con.disconnect();
				if(cons!=null)
					cons.disconnect();
			}
		}
		throw new Exception("Error code 404:\nInternet connection error");
	}

	/**
	 * Trust every server - do not check for any certificate
	 */
	public void TrustAllHosts() {
		/* Create a trust manager that does not validate certificate chains */
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {
				// TODO Auto-generated method stub
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
				// TODO Auto-generated method stub
			}
		} };

		/* Install the all-trusting trust manager */
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Always verify the host - do not check for certificate */
		HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);
	}

}
