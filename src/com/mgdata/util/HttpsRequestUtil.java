package com.mgdata.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.jd.open.api.sdk.internal.util.StringUtil;

/**
 * 公众平台通用接口工具类 用户实现https请求
 * 
 * @author golden
 * 
 */
public class HttpsRequestUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpsRequestUtil.class);

	/**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @param requestMethod
	 *            请求方式（GET、POST）
	 * @param param
	 *            提交的数据
	 * @param method
	 *            请求的接口名，用于更改在soap上面的SOAPAction里面的接口名
	 * @return 通过https请求得到的String result
	 */
	public static String httpRequest(String requestUrl, String requestMethod, String param, String method) {

		StringBuffer buffer = new StringBuffer();
		try {

			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());

			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			// 打开连接
			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();

			httpUrlConn.setRequestMethod(requestMethod);
			httpUrlConn.setRequestProperty("Host", "entservices.totalegame.net");
			httpUrlConn.setRequestProperty("Content-type", "text/xml; charset=utf-8");
			httpUrlConn.setRequestProperty("Content-Length", "length");
			httpUrlConn.setRequestProperty("SOAPAction", "https://entservices.totalegame.net/" + method);
			httpUrlConn.setSSLSocketFactory(ssf);
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);

			httpUrlConn.setRequestMethod(requestMethod);

			if ("POST".equalsIgnoreCase(requestMethod)) {
				httpUrlConn.connect();
			}
			// 当有数据需要提交时
			if (null != param) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(param.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			int resultCode = httpUrlConn.getResponseCode();
			// String str=httpUrlConn.getErrorStream();
			InputStream inputStream = null;
			if (resultCode != 200) {
				inputStream = httpUrlConn.getErrorStream();
			} else {
				inputStream = httpUrlConn.getInputStream();
			}
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();

		} catch (ConnectException ce) {

		} catch (Exception e) {

		}

		return buffer.toString();
	}

	/**
	 * 发起https请求JSON接口并获取结果
	 * 
	 * @param webUrl
	 *            请求MG的网址
	 * @param agent
	 *            代理的姓名
	 * @param password
	 *            代理 的密码
	 * @param requestMethod
	 *            请求方式（GET、POST）
	 * @param param
	 *            提交的数据 json对象的字符串
	 * @param method
	 *            请求方法 名称
	 * @return 通过https请求得到的JSONObject
	 */
	public static JSONObject httpsRequestJson(String webUrl, String agent, String password, String requestMethod, String method, String param) {
		JSONObject jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());

			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			String tempUrl = webUrl;
			if (!StringUtil.isEmpty(method)) {
				tempUrl += "/" + method;
			}

			URL url = new URL(tempUrl);

			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setSSLSocketFactory(ssf);

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
			httpUrlConn.setRequestProperty("Host", "entservices.totalegame.net");
			httpUrlConn.setRequestProperty("Content-type", "application/json");
			httpUrlConn.setRequestProperty("Content-Length", "length");
			httpUrlConn.setRequestProperty("Accept", "application/json");
			// String username = agent;
			// String password = password;
			String input = agent + ":" + password;
			String encoding = new sun.misc.BASE64Encoder().encode(input.getBytes());
			httpUrlConn.setRequestProperty("Authorization", "Basic " + encoding);
			// httpUrlConn.setRequestProperty("SOAPAction",
			// "https://entservices.totalegame.net/" + method);
			if (!StringUtil.isEmpty(requestMethod)) {
				httpUrlConn.connect();
			}

			logger.info("outputStr*********" + param);
			// 当有数据需要提交时
			if (null != param) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(param.getBytes("UTF-8"));
				outputStream.close();
			}
			int resultCode = httpUrlConn.getResponseCode();
			InputStream inputStream = null;
			if (resultCode != 200) {
				inputStream = httpUrlConn.getErrorStream();
			} else {
				inputStream = httpUrlConn.getInputStream();
			}

			// 将返回的输入流转换成字符串
			// InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			logger.info("responseCode: " + httpUrlConn.getResponseCode());
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			logger.info("buffer*********" + buffer);
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			jsonObject = JSONObject.fromObject(buffer.toString());
		} catch (ConnectException ce) {
			logger.info("ConnectException=." + ce.getMessage());
		} catch (Exception e) {
			logger.info("Exception=" + e.getMessage());
		}
		return jsonObject;
	}

	public static JSONArray httpRequestJson(String webUrl, String agent, String password, String requestMethod, String method, String param) {
		JSONArray jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		try {

			String tempUrl = webUrl;
			if (!StringUtil.isEmpty(method)) {
				tempUrl += "/" + method;
			}

			URL url = new URL(tempUrl);

			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
			httpUrlConn.setRequestProperty("Content-type", "application/json");

			httpUrlConn.setRequestProperty("Accept", "application/json");
			// String username = agent;
			// String password = password;
			String input = agent + ":" + password;
			String encoding = new sun.misc.BASE64Encoder().encode(input.getBytes());
			httpUrlConn.setRequestProperty("Authorization", "Basic " + encoding);
			// httpUrlConn.setRequestProperty("SOAPAction",
			// "https://entservices.totalegame.net/" + method);
			if (!StringUtil.isEmpty(requestMethod)) {
				httpUrlConn.connect();
			}

			logger.info("outputStr*********" + param);
			// 当有数据需要提交时
			if (null != param) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(param.getBytes("UTF-8"));
				outputStream.close();
			}
			int resultCode = httpUrlConn.getResponseCode();
			InputStream inputStream = null;
			if (resultCode != 200) {
				inputStream = httpUrlConn.getErrorStream();
			} else {
				inputStream = httpUrlConn.getInputStream();
			}

			// 将返回的输入流转换成字符串
			// InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			logger.info("responseCode: " + httpUrlConn.getResponseCode());
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			logger.info("buffer*********" + buffer);
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			jsonObject = JSONArray.fromObject(buffer.toString());
		} catch (ConnectException ce) {
			logger.info("ConnectException=." + ce.getMessage());
		} catch (Exception e) {
			logger.info("Exception=" + e.getMessage());
		}
		return jsonObject;
	}

	/**
	 * 登录mg
	 */
	public static String soapXMLReplace(String soapIsAuthenticateXMl, Map<String, Object> map) {
		String tempXml = soapIsAuthenticateXMl;
		if (!CollectionUtils.isEmpty(map)) {
			for (int i = 0; i < map.size(); i++) {
				tempXml = tempXml.replace("param" + i, "" + map.get("param" + i));
			}

		}
		return tempXml;
	}

	// 以下的程序是在SOAP 返回的XML里面找相应的值
	public static String getXMLValue(String XMLvalue, String node) throws DocumentException {
		Document document = DocumentHelper.parseText(XMLvalue);
		Element root = document.getRootElement();// 获取根节点
		// getNodes(root);// 从根节点开始遍历所有节点
		List<Element> list = root.elements();
		String result = getNode(root, node, null);
		return result;

	}

	// 以下的程序是在XML中找相就的结点，如果结点的值与所要的接点相同时，直接返回接点的值
	public static String getNode(Element node, String nodeString, Element nodeFound) {
		String result = "";
		if (nodeFound != null) {
			return result;
		} else {
			// 当前节点下面子节点迭代器
			Iterator<Element> it = node.elementIterator();
			while ((it.hasNext()) && (nodeFound == null)) {

				Element e = it.next();
				if (e.getName().equals(nodeString)) {
					nodeFound = e;
					result = e.getTextTrim();
				} else {
					result = getNode(e, nodeString, nodeFound);
				}
			}
		}
		return result;
	}

}