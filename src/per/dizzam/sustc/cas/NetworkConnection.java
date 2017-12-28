package per.dizzam.sustc.cas;

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

/**
 * This class provided basic network connection to any service needing CAS login
 * 
 * @author Zeyu DONG
 *
 */
public class NetworkConnection {

	/** This is the default CAS login page: {@value} */
	public static final String URL_CAS = "https://cas.sustc.edu.cn/cas/login";

	/**
	 * This is the url of service. It must be set before using this class
	 * 
	 * @see #setUrl(String)
	 * @see #NetworkConnection(String)
	 */
	protected String url = "";

	/** Username of CAS */
	protected String username = "";

	/** Password of CAS */
	protected String password = "";

	private CloseableHttpClient httpclient;
	private CookieStore cookieStore;
	private boolean isLogin = false;
	private Logger logger = Logger.getLogger("Network");

	/**
	 * In the constructor, the basic httpclient was set, but will not login CAS
	 * before you use {@link #login()}
	 * 
	 * @param url
	 *            set field of {@link #url}
	 */
	public NetworkConnection(String url) {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		cookieStore = new BasicCookieStore();
		setupSSL();// 建立通过SSL的httpclient
	}

	/**
	 * You need not set {@link #url} if you extended this calss
	 * 
	 * @see #NetworkConnection(String)
	 */
	protected NetworkConnection() {
		this("");
	}

	private void setupSSL() {
		try {
			SSLContext sslContext;
			sslContext = SSLContexts.custom()
					.loadTrustMaterial(this.getClass().getResource("/cas.keystore"), "123456".toCharArray()).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1" },
					null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());// 证书认证
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf)
					.setUserAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
					.setDefaultCookieStore(cookieStore).build();// 客户端建立
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException
				| IOException e) {
			logger.fatal(e.getMessage(), e);
			System.exit(-1);
		}
	}

	/**
	 * set the service url
	 * 
	 * @see #url
	 * 
	 * @param url
	 *            set {@link #url}
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * set the username and password of CAS
	 * 
	 * @see #username
	 * @see #password
	 * 
	 * @param username
	 *            set {@link #username}
	 * @param password
	 *            set {@link #password}
	 */
	public void setIndentity(String username, String password) {
		this.username = username;
		this.password = password;
	}

	private void loginCAS() throws AuthenticationException, IOException {
		HttpGet get = new HttpGet(URL_CAS);
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == 200) {// 返回认证页面
				Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
				response.close();
				String lt = document.getElementsByAttributeValue("name", "lt").attr("value");
				String execution = document.getElementsByAttributeValue("name", "execution").attr("value");
				HttpPost post = new HttpPost(URL_CAS);
				post.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());
				CloseableHttpResponse response1 = null;
				post.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("username", username));
				nvps.add(new BasicNameValuePair("password", password));
				nvps.add(new BasicNameValuePair("lt", lt));
				nvps.add(new BasicNameValuePair("execution", execution));
				nvps.add(new BasicNameValuePair("_eventId", "submit"));
				nvps.add(new BasicNameValuePair("submit", "LOGIN"));
				post.setEntity(new UrlEncodedFormEntity(nvps));
				response1 = httpclient.execute(post);
				if (response1 != null && response1.toString().contains("TGC")) {
					logger.info("Login Succeed!");
				} else {
					response1.close();
					throw new AuthenticationException("Login Failed: Error authentication information");
				}
				response1.close();
			}
		} catch (ClientProtocolException | UnsupportedEncodingException e) {
			logger.fatal(e.getMessage(), e);
		}
	}

	/**
	 * this method call {@link #dataFetcher(Method, String, String[], boolean)}
	 * with dataFetcher(type, suburl, null, false)
	 * 
	 * @see #dataFetcher(Method, String, String[], boolean)
	 * @param type
	 * @param suburl
	 * @param postdata
	 * @return the response
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	public CloseableHttpResponse dataFetcher(Method type, String suburl) throws AuthenticationException, IOException {
		return dataFetcher(type, suburl, null, false);
	}

	/**
	 * this method call {@link #dataFetcher(Method, String, String[], boolean)}
	 * with dataFetcher(type, suburl, null, setRedirect)
	 * 
	 * @see #dataFetcher(Method, String, String[], boolean)
	 * @param type
	 * @param suburl
	 * @param postdata
	 * @return the response
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	public CloseableHttpResponse dataFetcher(Method type, String suburl, boolean setRedirect)
			throws AuthenticationException, IOException {
		return dataFetcher(type, suburl, null, setRedirect);
	}

	/**
	 * this method call {@link #dataFetcher(Method, String, String[], boolean)}
	 * with dataFetcher(type, suburl, postdata, false)
	 * 
	 * @see #dataFetcher(Method, String, String[], boolean)
	 * @param type
	 * @param suburl
	 * @param postdata
	 * @return the response or {@code null} if error in format
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	public CloseableHttpResponse dataFetcher(Method type, String suburl, String[] postdata)
			throws AuthenticationException, IOException {
		return dataFetcher(type, suburl, postdata, false);
	}

	/**
	 * Make the POST or GET request to the url <br />
	 * <br />
	 * It will automatically deal with the matter such as CAS login, but you
	 * must close the response afterwards <br />
	 * <br />
	 * You can set the {@code postdata} if the method is POST <br />
	 * <br />
	 * If you set the {@code setRedirect} to true, the method will not throw
	 * {@code AuthenticationException} even if you cannot login. instead, you
	 * have to determine by yourself
	 * 
	 * @param type
	 *            support only GET and POST
	 * @param suburl
	 *            the full url of the request is {@link #url} + suburl
	 * @param postdata
	 *            the data to be post to the server. it must strictly follow the
	 *            form "{@code parameter=vaule}" in each String
	 * @param setRedirect
	 *            indicate whether the client deal with redirect automatically
	 * 
	 * @return the response or {@code null} if error in format
	 * 
	 * @throws AuthenticationException
	 *             if CAS login failed
	 * @throws IOException
	 *             if network error
	 * 
	 * @see {@link Method}
	 * @see CloseableHttpResponse
	 */
	public CloseableHttpResponse dataFetcher(Method type, String suburl, String[] postdata, boolean setRedirect)
			throws AuthenticationException, IOException {
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
			} else if (type == Method.POST) {
				HttpPost opr = new HttpPost(url + suburl);
				opr.setConfig(RequestConfig.custom().setRedirectsEnabled(setRedirect).build());
				opr.addHeader(new BasicHeader("Connection", "Keep-Alive"));
				if (postdata != null) {
					List<NameValuePair> nvps = new ArrayList<NameValuePair>();
					for (int i = 0; i < postdata.length; i++) {
						String string = postdata[i];
						if (string.split("=").length == 2) {
							nvps.add(new BasicNameValuePair(string.split("=")[0], string.split("=")[1]));
						} else if (string.split("=").length == 1) {
							nvps.add(new BasicNameValuePair(string.split("=")[0], ""));
						} else {
							return null;
						}
					}
					opr.setEntity(new UrlEncodedFormEntity(nvps));
				}
				response = httpclient.execute(opr);
			} else {
				return null;
			}
			if (response.getStatusLine().getStatusCode() == 302
					&& response.getHeaders("Location")[0].getValue().startsWith(URL_CAS)) {
				login();
				return dataFetcher(type, suburl, postdata, true);
			}
			return response;
		} catch (ClientProtocolException | UnsupportedEncodingException e) {
			logger.fatal(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * login CAS using current username and password
	 * 
	 * @throws AuthenticationException
	 *             if the identity is error or some other error (Network Error)
	 *             occurred when doing login
	 */
	protected void login() throws AuthenticationException {
		logger.info("Login...");
		clear();
		try {
			loginCAS();
			isLogin = true;
			dataFetcher(Method.GET, "/", true).close();
		} catch (IOException e) {
			logger.warn("Network error! Please check you have access to the Internet.");
			isLogin = false;
			throw new AuthenticationException("Can't connect to Central Authentication Servives(CAS)", e);
		} catch (AuthenticationException e) {
			isLogin = false;
			throw e;
		}
	}

	/**
	 * @return if login CAS succeed
	 */
	public boolean isLogin() {
		return isLogin;
	}

	private void clear() {
		cookieStore.clear();
		isLogin = false;
	}
}
