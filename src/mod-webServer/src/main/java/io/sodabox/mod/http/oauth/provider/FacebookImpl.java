package io.sodabox.mod.http.oauth.provider;

import io.sodabox.mod.http.oauth.OAuthConfig;
import io.sodabox.mod.http.oauth.Profile;
import io.sodabox.mod.http.oauth.exception.ServerDataException;
import io.sodabox.mod.http.oauth.exception.SocialAuthException;
import io.sodabox.mod.http.oauth.exception.UserDeniedPermissionException;
import io.sodabox.mod.http.oauth.strategy.OAuth2;
import io.sodabox.mod.http.oauth.strategy.OAuthStrategyBase;
import io.sodabox.mod.http.oauth.strategy.RequestToken;
import io.sodabox.mod.http.oauth.utils.AccessGrant;
import io.sodabox.mod.http.oauth.utils.BirthDate;
import io.sodabox.mod.http.oauth.utils.Constants;
import io.sodabox.mod.http.oauth.utils.Response;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class FacebookImpl extends AbstractProvider implements AuthProvider{

	private static final String PROFILE_URL = "https://graph.facebook.com/me";
	private static final String PROFILE_IMAGE_URL = "http://graph.facebook.com/%1$s/picture";
	private static final Map<String, String> ENDPOINTS;

	private OAuthConfig config;
	private OAuthStrategyBase authenticationStrategy;

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(
				Constants.OAUTH_AUTHORIZATION_URL,
				"https://graph.facebook.com/oauth/authorize");
		ENDPOINTS.put(
				Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://graph.facebook.com/oauth/access_token");
	}

	public FacebookImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
		authenticationStrategy = new OAuth2(config, ENDPOINTS);
	}

	@Override
	public RequestToken getLoginRedirectURL() throws Exception {
		return authenticationStrategy.getLoginRedirectURL();
	}

	@Override
	public Profile connect(AccessGrant requestToken, Map<String, String> requestParams) throws Exception {
		if (requestParams.get("error_reason") != null
				&& "user_denied".equals(requestParams.get("error_reason"))) {
			throw new UserDeniedPermissionException();
		}
		AccessGrant accessGrant = authenticationStrategy.verifyResponse(null, requestParams);

		if (accessGrant != null) {

			return authFacebookLogin(accessGrant);
		} else {
			throw new SocialAuthException("Access token not found");
		}
	}

	private Profile authFacebookLogin(AccessGrant accessGrant) throws Exception {
		String presp;

		try {
			Response response = authenticationStrategy.executeFeed(accessGrant, PROFILE_URL);
			presp = response.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception e) {
			throw new SocialAuthException("Error while getting profile from "
					+ PROFILE_URL, e);
		}
		try {
			System.out.println("User Profile : " + presp);
			JSONObject resp = new JSONObject(presp);
			Profile p = new Profile();
			//p.setValidatedId(resp.getString("id"));
			//p.setFirstName(resp.getString("first_name"));
			//p.setLastName(resp.getString("last_name"));
			//p.setEmail(resp.getString("email"));
			if (resp.has("location")) {
				//p.setLocation(resp.getJSONObject("location").getString("name"));
			}
			if (resp.has("birthday")) {
				String bstr = resp.getString("birthday");
				String[] arr = bstr.split("/");
				BirthDate bd = new BirthDate();
				if (arr.length > 0) {
					bd.setMonth(Integer.parseInt(arr[0]));
				}
				if (arr.length > 1) {
					bd.setDay(Integer.parseInt(arr[1]));
				}
				if (arr.length > 2) {
					bd.setYear(Integer.parseInt(arr[2]));
				}
				//p.setDob(bd);
			}
			if (resp.has("gender")) {
				//p.setGender(resp.getString("gender"));
			}
			//p.setProfileImageURL(String.format(PROFILE_IMAGE_URL,
			//		resp.getString("id")));
			String locale = resp.getString("locale");
			if (locale != null) {
				String a[] = locale.split("_");
				//p.setLanguage(a[0]);
				//p.setCountry(a[1]);
			}
			//p.setProviderId(getProviderId());
			if (resp.has("link")) {
				p.setLink(resp.getString("link"));
			}
			if (resp.has("name")) {
				p.setName(resp.getString("name"));
			}else{
				p.setName(resp.getString("first_name")+" " +resp.getString("last_name"));
			}
			
			
			
			return p;

		} catch (Exception ex) {
			throw new ServerDataException(
					"Failed to parse the user profile json : " + presp, ex);
		}
	}

	@Override
	public String getProviderId() {
		return config.getId();
	}

}