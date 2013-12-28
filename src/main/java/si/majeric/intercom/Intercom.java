package si.majeric.intercom;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Intercom {
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String POST_CONTENT_TYPE = "application/json";
	/**
	 * The Intercom API endpoint
	 */

	private static URI API_ENDPOINT;

	/**
	 * The Intercom application ID
	 */
	private final String _appId;

	/**
	 * The Intercom API key
	 */
	private final String _apiKey;

	/**
	 * Last HTTP error obtained from curl_errno() and curl_error()
	 */
	private String _lastError;

	static {
		try {
			API_ENDPOINT = new URI("https://api.intercom.io/v1/");
		} catch (URISyntaxException e) {
			API_ENDPOINT = null;
			e.printStackTrace();
		}
	}

	/**
	 * The constructor
	 * 
	 * @param string
	 *            appId The Intercom application ID
	 * @param string
	 *            apiKey The Intercom API key
	 * @param string
	 *            debug Optional debug flag
	 * @return void
	 * 
	 */
	public Intercom(String appId, String apiKey) {
		this._appId = appId;
		this._apiKey = apiKey;
	}

	/**
	 * Check if a given value is an e-mail address.
	 * 
	 * @param string
	 *            value
	 * @return boolean
	 * 
	 */
	private boolean isEmail(String value) {
		return Pattern.compile(EMAIL_PATTERN).matcher(value).matches();
	}

	private JsonElement httpCall(String url) {
		return httpCall(url, "GET", null);
	}

	private JsonElement httpCall(String url, String method) {
		return httpCall(url, method, null);
	}

	/**
	 * Make an HTTP call using curl.
	 * 
	 * @param string
	 *            url The URL to call
	 * @param string
	 *            method The HTTP method to use, by default GET
	 * @param string
	 *            post_data The data to send on an HTTP POST (optional)
	 * @return object
	 * 
	 */
	private JsonElement httpCall(String url, String method, String postData) {
		if (method == null) {
			method = "GET";
		}
		// String[] headers = { "Content-Type: application/json" };

		HttpHost targetHost = new HttpHost(API_ENDPOINT.getHost(), API_ENDPOINT.getPort(), API_ENDPOINT.getScheme());

		CloseableHttpClient httpclient = null;
		try {
			URI uri = URI.create(url);
			HttpRequestBase httpRequest = prepareRequest(uri, postData, method);

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials(_appId, _apiKey));

			httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			// Generate BASIC scheme object and add it to the local auth cache
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(targetHost, basicAuth);

			// Add AuthCache to the execution context
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setAuthCache(authCache);

			CloseableHttpResponse response = httpclient.execute(targetHost, httpRequest, localContext);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String content = EntityUtils.toString(entity);
					JsonParser parser = new JsonParser();
					return parser.parse(content);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpclient != null) {
					httpclient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// ch = curl_init(url);
		//
		// if (this.debug) {
		// curl_setopt(ch, CURLOPT_VERBOSE, true);
		// }
		//
		// if (method == "POST") {
		// curl_setopt(ch, CURLOPT_POSTFIELDS, post_data);
		// curl_setopt(ch, CURLOPT_POST, true);
		// } else if (method == "PUT") {
		// curl_setopt(ch, CURLOPT_CUSTOMREQUEST, "PUT");
		// curl_setopt(ch, CURLOPT_POSTFIELDS, post_data);
		// headers[] = "Content-Length: " . strlen(post_data);
		// } else if (method != "GET") {
		// curl_setopt(ch, CURLOPT_CUSTOMREQUEST, method);
		// }
		// curl_setopt(ch, CURLOPT_RETURNTRANSFER, true);
		// curl_setopt(ch, CURLOPT_HTTPHEADER, headers);
		// curl_setopt(ch, CURLOPT_CONNECTTIMEOUT, 5);
		// curl_setopt(ch, CURLOPT_TIMEOUT, 600); // Intercom doesn"t support pagination, some calls are slow
		// curl_setopt(ch, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
		// curl_setopt(ch, CURLOPT_USERPWD, this.appId . ":" . this.apiKey);
		//
		//
		// // Set HTTP error, if any
		// this.lastError = array(
		// "code" => curl_errno(ch),
		// "message" => curl_error(ch)
		// );

		return null;
	}

	private HttpRequestBase prepareRequest(URI url, String data, final String method) throws UnsupportedEncodingException {
		HttpEntityEnclosingRequestBase request = new HttpEntityEnclosingRequestBase() {
			@Override
			public String getMethod() {
				return method;
			}
		};
		request.setURI(url);
		request.setHeader("Accept", POST_CONTENT_TYPE);

		/* convert the request object to a string that will be sent in the post */
		if (data != null) {
			StringEntity entity = new StringEntity(data, "UTF-8");
			entity.setContentType(POST_CONTENT_TYPE);
			request.setEntity(entity);
		}

		return request;
	}

	/**
	 * Get all users from your Intercom account.
	 * 
	 * @param integer
	 *            page The results page number
	 * @param integer
	 *            perPage The number of results to return on each page
	 * @return object
	 * 
	 */
	public JsonElement getAllUsers(Integer page, Integer perPage) {
		String path = "users/?page=" + page;

		if (perPage != null) {
			path += "&per_page=" + perPage;
		}

		return this.httpCall(API_ENDPOINT + path);
	}

	/**
	 * Get a specific user from your Intercom account.
	 * 
	 * @param string
	 *            id The ID of the user to retrieve
	 * @return object
	 * 
	 */
	public JsonElement getUser(String id) {
		String path = "users/";
		if (this.isEmail(id)) {
			path += "?email=";
		} else {
			path += "?user_id=";
		}
		try {
			path += URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			path += id;
			e.printStackTrace();
		}
		return this.httpCall(API_ENDPOINT + path);
	}

	/**
	 * Get the message thread of a specific user from your Intercom account.
	 * 
	 * @param string
	 *            id The ID of the user to retrieve thread for
	 * @return object
	 * 
	 */
	public JsonElement getThread(String id) {
		String path = "users/message_threads";
		if (this.isEmail(id)) {
			path += "?email=";
		} else {
			path += "?user_id=";
		}
		try {
			path += URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			path += id;
			e.printStackTrace();
		}
		return this.httpCall(API_ENDPOINT + path);
	}

	/**
	 * Create a user on your Intercom account.
	 * 
	 * @param string
	 *            id The ID of the user to be created
	 * @param string
	 *            email The user"s email address (optional)
	 * @param string
	 *            name The user"s name (optional)
	 * @param array
	 *            customData Any custom data to be aggregate to the user"s record (optional)
	 * @param long createdAt UNIX timestamp describing the date and time when the user was created (optional)
	 * @param string
	 *            lastSeenIp The last IP address where the user was last seen (optional)
	 * @param string
	 *            lastSeenUserAgent The last user agent of the user"s browser (optional)
	 * @param long lastRequestAt UNIX timestamp of the user"s last request (optional)
	 * @param array
	 *            increments Any increments data to be aggregate to the user"s record (optional)
	 * @param string
	 *            method HTTP method, to be used by updateUser()
	 * @return object
	 * 
	 */
	public JsonElement createUser(String id, String email, String name, String[] customData, Long createdAt, String lastSeenIp, String lastSeenUserAgent,
			Long lastRequestAt, String increments) {
		return this.createUser(id, email, name, customData, createdAt, lastSeenIp, lastSeenUserAgent, lastRequestAt, increments, "POST");
	}

	private JsonElement createUser(String id, String email, String name, String[] customData, Long createdAt, String lastSeenIp, String lastSeenUserAgent,
			Long lastRequestAt, String increments, String method) {
		Map<String, Object> data = new HashMap<String, Object>();

		if (id != null) {
			data.put("user_id", id);
		}

		if (email != null) {
			data.put("email", email);
		}

		if (name != null) {
			data.put("name", name);
		}

		if (createdAt != null) {
			data.put("created_at", createdAt);
		}

		if (lastSeenIp != null) {
			data.put("last_seen_ip", lastSeenIp);
		}

		if (lastSeenUserAgent != null) {
			data.put("last_seen_user_agent", lastSeenUserAgent);
		}

		if (lastRequestAt != null) {
			data.put("last_request_at", lastRequestAt);
		}

		if (customData != null) {
			data.put("custom_data", customData);
		}

		if (increments != null) {
			data.put("increments", increments);
		}

		String path = "users";
		return this.httpCall(API_ENDPOINT + path, method, new Gson().toJson(data));
	}

	/**
	 * Update an existing user on your Intercom account.
	 * 
	 * @param string
	 *            id The ID of the user to be updated
	 * @param string
	 *            email The user"s email address (optional)
	 * @param string
	 *            name The user"s name (optional)
	 * @param array
	 *            customData Any custom data to be aggregate to the user"s record (optional)
	 * @param long createdAt UNIX timestamp describing the date and time when the user was created (optional)
	 * @param string
	 *            lastSeenIp The last IP address where the user was last seen (optional)
	 * @param string
	 *            lastSeenUserAgent The last user agent of the user"s browser (optional)
	 * @param long lastRequestAt UNIX timestamp of the user"s last request (optional)
	 * @return object
	 * 
	 */
	public Object updateUser(String id, String email, String name, String[] customData, Long createdAt, String lastSeenIp, String lastSeenUserAgent,
			Long lastRequestAt, String increments) {
		return this.createUser(id, email, name, customData, createdAt, lastSeenIp, lastSeenUserAgent, lastRequestAt, increments, "PUT");
	}

	/**
	 * Delete an existing user from your Intercom account
	 * 
	 * @param string
	 *            id The ID of the user to be deleted
	 * @return object
	 * 
	 */
	public JsonElement deleteUser(String id) {
		String path = "users/";
		if (this.isEmail(id)) {
			path += "?email=";
		} else {
			path += "?user_id=";
		}
		try {
			path += URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			path += id;
			e.printStackTrace();
		}
		return this.httpCall(API_ENDPOINT + path, "DELETE");
	}

	/**
	 * Create an impression associated with a user on your Intercom account
	 * 
	 * @param string
	 *            userId The ID of the user
	 * @param string
	 *            email The email of the user (optional)
	 * @param string
	 *            userIp The IP address of the user (optional)
	 * @param string
	 *            userAgent The user agent of the user (optional)
	 * @param string
	 *            currentUrl The URL the user is visiting (optional)
	 * @return object
	 * 
	 */
	public JsonElement createImpression(String userId, String email, String userIp, String userAgent, String currentUrl) {
		Map<String, Object> data = new HashMap<String, Object>();

		data.put("user_id", userId);

		if (email != null) {
			data.put("email", email);
		}

		if (userIp != null) {
			data.put("user_ip", userIp);
		}

		if (userAgent != null) {
			data.put("user_agent", userAgent);
		}

		if (currentUrl != null) {
			data.put("current_url", currentUrl);
		}
		String path = "users/impressions";

		return this.httpCall(API_ENDPOINT + path, "POST", new Gson().toJson(data));
	}

	/**
	 * Get the last error from curl.
	 * 
	 * @return array Array with "code" and "message" indexes
	 */
	public String getLastError() {
		return _lastError;
	}

	/**
	 * Get a specific tag from your Intercom account.
	 * 
	 * @param string
	 *            name The Name of the tag to retrieve
	 * @return object
	 * 
	 */
	public JsonElement getTag(String name) {
		String path = "tags/";
		try {
			path += "?name=" + URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			path += "?name=" + name;
			e.printStackTrace();
		}
		return this.httpCall(API_ENDPOINT + path);
	}

	/**
	 * Create a tag on your Intercom account.
	 * 
	 * @param string
	 *            name The tag"s name (required)
	 * @param array
	 *            emails Array of users to tag (optional)
	 * @param array
	 *            userIds Array of user ids to tag (optional)
	 * @param string
	 *            color The color of the tag (must be "green", "red", "teal", "gold", "blue", or "purple").
	 * @param string
	 *            action required (if emails or userIds are not empty) — either "tag" or "untag"
	 * @param string
	 *            method HTTP method, to be used by updateTag()
	 * 
	 */
	public JsonElement createTag(String name) {
		return createTag(name, null, null, null, null, "POST");
	}

	public JsonElement createTag(String name, String emails, String userIds, String color, String action, String method) {
		Map<String, Object> data = new HashMap<String, Object>();

		data.put("name", name);

		if (emails != null) {
			data.put("email", emails);
		}

		if (action != null) {
			data.put("tag_or_untag", action);
		}

		if (emails != null) {
			data.put("emails", emails);
		}

		if (userIds != null) {
			data.put("user_ids", userIds);
		}

		if (color != null) {
			data.put("color", color);
		}

		String path = "tags";
		return this.httpCall(API_ENDPOINT + path, method, new Gson().toJson(data));
	}

	/**
	 * Create a tag on your Intercom account.
	 * 
	 * @param string
	 *            name The tag"s name (required)
	 * @param array
	 *            emails Array of users to tag (optional)
	 * @param array
	 *            userIds Array of user ids to tag (optional)
	 * @param string
	 *            color The color of the tag (must be "green", "red", "teal", "gold", "blue", or "purple").
	 * @param string
	 *            action required (if emails or userIds are not empty) — either "tag" or "untag"
	 * 
	 */
	public JsonElement updateTag(String name, String emails, String userIds, String color, String action) {
		return this.createTag(name, emails, userIds, color, action, "PUT");
	}
}
