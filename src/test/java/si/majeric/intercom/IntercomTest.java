package si.majeric.intercom;

import java.util.Iterator;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class IntercomTest extends TestCase {
	private static final int PAGE_SIZE = 100;

	private static final Logger logger = LoggerFactory.getLogger(IntercomTest.class);

	private Intercom _intercom;

	protected void setUp() throws Exception {
		String appId = "{your-app-id}";
		String apiKey = "{your-api-key}";
		_intercom = new Intercom(appId, apiKey);
	}

	public void testGetAllUsers() {
		if (_intercom == null) {
			logger.warn("Intercom not initialized!");
		}
		Integer page = 1;
		do {
			JsonElement response = _intercom.getAllUsers(page, PAGE_SIZE);
			if (response != null && response.isJsonObject()) {
				JsonObject responseAsJO = response.getAsJsonObject();
				logger.info("Retrieving " + responseAsJO.get("page").getAsString() + " of " + responseAsJO.get("total_pages").getAsString());
				JsonElement users = responseAsJO.get("users");
				if (users != null && users.isJsonArray()) {
					Iterator<JsonElement> iterator = users.getAsJsonArray().iterator();
					while (iterator.hasNext()) {
						JsonObject user = iterator.next().getAsJsonObject();
						if (user.get("email") != null && !user.get("email").isJsonNull()) {
							String email = user.get("email").getAsString();
							logger.info("User with email {} registered.", email);
						}
					}
				} else {
					if (responseAsJO.get("error") != null) {
						logger.error("IntercomDataSource error: " + responseAsJO.get("error"));
					}
				}
				if (responseAsJO.get("next_page").isJsonNull()) {
					page = null;
				} else {
					page = responseAsJO.get("next_page").getAsInt();
				}
			} else {
				logger.error("There was an issue fetcing data from intercom: " + _intercom.getLastError());
				break;
			}
		} while (page != null);
	}
}
