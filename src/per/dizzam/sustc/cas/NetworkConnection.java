package per.dizzam.sustc.cas;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
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
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class NetworkConnection {
	private CloseableHttpClient httpclient;
	public static final String url_cas = "https://cas.sustc.edu.cn/cas/login";
	protected String url = "";
	private CookieStore cookieStore;
	protected String username = "";
	protected String password = "";
	private boolean isLogin = false;
	private Logger logger = Logger.getLogger("Network");
		
	public NetworkConnection() {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		cookieStore = new BasicCookieStore();
		setupSSL();//建立通过SSL的httpclient
	}
	
	private void setupSSL() {
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
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException e) {
			logger.fatal(e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			logger.fatal("Load cert failed. Exit");
			System.exit(-1);
		}
	}

	private void loginCAS() throws AuthenticationException, IOException {
		HttpGet get = new HttpGet(url_cas);
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == 200) {//返回认证页面
				Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
				response.close();
				String lt = document.getElementsByAttributeValue("name", "lt").attr("value");
				String execution = document.getElementsByAttributeValue("name", "execution").attr("value");
				HttpPost post = new HttpPost(url_cas);
				post.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());
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
				if(response1 != null && response1.toString().contains("TGC")){
					logger.info("Login Succeed!");
				}
				else{
					if(EntityUtils.toString(response1.getEntity()).contains("class=\"errors\"")) {
						throw new AuthenticationException("Login Failed: Error authentication information");
					}
				}
				response1.close();
			}
		} catch (ClientProtocolException | UnsupportedEncodingException e) {
			logger.fatal(e.getMessage());
			System.exit(-1);
		}
	}
	
	public CloseableHttpResponse dataFetcher(Method type, String suburl) throws AuthenticationException, IOException {
		return dataFetcher(type, suburl, null);
	}
	
	public CloseableHttpResponse dataFetcher(Method type, String suburl, boolean setRedirect) throws AuthenticationException, IOException {
		return dataFetcher(type, suburl, null, setRedirect);
	}
	
	public CloseableHttpResponse dataFetcher(Method type, String suburl, String[] postdata) throws AuthenticationException, IOException {
		return dataFetcher(type, suburl, postdata, false);
	}
	
	public CloseableHttpResponse dataFetcher(Method type, String suburl, String[] postdata, boolean setRedirect) throws AuthenticationException, IOException {//post data has the form: name=value
		if (!isLogin) {
			login();
		}
		try {
			CloseableHttpResponse response;
			if (type == Method.GET) {
				HttpGet opr = new HttpGet(url + suburl);
				opr.setConfig(RequestConfig.custom().setRedirectsEnabled(setRedirect).build());
				opr.addHeader(new BasicHeader("Connection", "Keep-Alive"));
				response = httpclient.execute(opr);
			}
			else if (type == Method.POST) {
				HttpPost opr = new HttpPost(url + suburl);
				opr.setConfig(RequestConfig.custom().setRedirectsEnabled(setRedirect).build());
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
			} else {
				return null;
			}
			if (response.getStatusLine().getStatusCode() == 302 && 
					response.getHeaders("Location")[0].getValue().startsWith(url_cas)) {
				login();
				return dataFetcher(type, suburl, postdata, true);
			}
			return response;
		} catch (ClientProtocolException | UnsupportedEncodingException e) {
			logger.fatal(e.getMessage());
			System.exit(-1);
		}
		return null;
	}
	
	protected void login() throws AuthenticationException {
		logger.info("Login...");
		clear();
		try {
			loginCAS();
			isLogin = true;
			dataFetcher(Method.GET, "/", true);
		} catch (IOException e) {
			logger.warn("Network error! Please check you have access to the Internet.");
			throw new AuthenticationException("Can't connect to Central Authentication Servives(CAS)", e);
		}
	}
	
	public boolean isLogin() {
		return isLogin;
	}
	
	protected void clear() {
		cookieStore.clear();
		isLogin = false;
	}
	
}
