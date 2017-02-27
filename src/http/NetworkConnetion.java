package http;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class NetworkConnetion {
	private CloseableHttpClient httpclient;
	private final String url_cas = "https://cas.sustc.edu.cn/cas/login?service=http://jwxt.sustc.edu.cn/jsxsd/";
	protected String url = "";
	private CookieStore cookieStore;
	public static final int GET = 1;
	public static final int POST =2;
	protected String username = "";
	protected String password = "";
	private String lt = "";
	private String execution = "";	
	private String ticket = "";
	private String TGC = "";
		
	public NetworkConnetion() {
		setupSSL();//建立通过SSL的httpclient
	}
	
	private void setupSSL() {
		cookieStore = new BasicCookieStore();
		try {
			SSLContext sslContext;
			sslContext = SSLContexts.custom().loadTrustMaterial(new File("cas.keystore"), "123456".toCharArray()).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext,
					new String[] { "TLSv1" },
					null,
					SSLConnectionSocketFactory.getDefaultHostnameVerifier());//证书认证
			httpclient = HttpClients.custom()
					.setSSLSocketFactory(sslsf)
					.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
					.setDefaultCookieStore(cookieStore)
					.build();//客户端建立
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean loginCAS() throws IOException {
		boolean re = false;
		String cas = url_cas + "?service=" + url;
		HttpGet get = new HttpGet(cas);	
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == 200) {//返回认证页面
				Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
				response.close();
				lt = document.getElementsByAttributeValue("name", "lt").attr("value");
				execution = document.getElementsByAttributeValue("name", "execution").attr("value");
				HttpPost post = new HttpPost(cas);
				CloseableHttpResponse response1 = null;
				post.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));		
				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair("username", username));
				nvps.add(new BasicNameValuePair("password", password));
				nvps.add(new BasicNameValuePair("lt", lt));            
				nvps.add(new BasicNameValuePair("execution", execution));
				nvps.add(new BasicNameValuePair("_eventId", "submit"));
				nvps.add(new BasicNameValuePair("submit", "LOGIN"));
				post.setEntity(new UrlEncodedFormEntity(nvps));
				response1 = httpclient.execute(post);
				if(response1 != null && response1.getStatusLine().getStatusCode() == 302){
					TGC = response1.getHeaders("Set-Cookie")[1].getValue().split(";")[0];
					ticket = response1.getHeaders("Location")[0].getValue().split("&")[1];
					System.out.println("[NetWork] Login Succeed!");
					System.out.println("[NetWork] " + ticket
//							.replaceAll("-(.*?)-cas", "-***********-cas")
							);
					re = true;
				}
				else{
					re = false;
				}
				response1.close();
			}			
		}catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}
	
	private boolean jwxtJSessionVerify() throws IOException {
		HttpGet get = new HttpGet(url + "/jsxsd/" + "?" + ticket);
		boolean re = false;
		CloseableHttpResponse response;
		if (loginCAS()) {
			try {
				response = httpclient.execute(get);
				if (response.getStatusLine().getStatusCode() == 200 || 
						(response.getStatusLine().getStatusCode() == 302 && 
						!(response.getHeaders("Location")[0].getValue().startsWith(url_cas)))) {
					System.out.println("[NetWork] Verify OK");
					re = true;
				}
				response.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			return false;
		}
		return re;
	}
	
	public CloseableHttpResponse dataFetcher(int type, String suburl, String[] postdata) {//post data has the form: name=value
		CloseableHttpResponse response = null;
//		opr.addHeader(new BasicHeader("X-Requested-With", "XMLHttpRequest"));//unused
		if (!(isLogIn() || login())) {
			return null;
		}
		try {
			if (type == GET) {
				HttpGet opr = new HttpGet(url + suburl);
				opr.addHeader(new BasicHeader("Connection", "Keep-Alive"));
				response = httpclient.execute(opr);
			}
			else if (type == POST) {
				HttpPost opr = new HttpPost(url + suburl);
				opr.addHeader(new BasicHeader("Connection", "Keep-Alive"));
				if (postdata != null) {
					List<NameValuePair> nvps = new ArrayList<NameValuePair>();
					for (int i = 0; i < postdata.length; i++) {
						String string = postdata[i];
						if (string.split("=").length == 2) {
							nvps.add(new BasicNameValuePair(string.split("=")[0], string.split("=")[1]));
						}
						else if (string.split("=").length ==1 ) {
							nvps.add(new BasicNameValuePair(string.split("=")[0], ""));
						}
						else {
							return null;
						}
					}
					opr.setEntity(new UrlEncodedFormEntity(nvps));
				}
				response = httpclient.execute(opr);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (response == null) {
			return response;
		}
		else if (response.getStatusLine().getStatusCode() == 403 || 
				(response.getStatusLine().getStatusCode() == 302 && 
				response.getHeaders("Location")[0].getValue().startsWith(url_cas))) {
			if (!login()) {
				return null;
			}
			return dataFetcher(type, suburl, postdata);
		}
		return response;
	}
	
	protected boolean login() {
		System.out.println("[NetWork] Login...");
		clear();
		try {
			if (jwxtJSessionVerify()) {
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("[NetWork] Network error! Please check you have access to the Internet.");
			return false;
		}
		System.out.println("[NetWork] Login Failed. Please check your username and password.");
		return false;
	}
	
	protected void clear() {
		lt = "";
		execution = "";	
		ticket = "";
		TGC = "";
		cookieStore.clear();
	}
	
	public boolean isLogIn() {
		if (TGC == "") {
			return false;
		}
		return true;
	}
	
}
