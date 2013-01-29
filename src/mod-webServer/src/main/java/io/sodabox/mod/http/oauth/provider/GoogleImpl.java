package io.sodabox.mod.http.oauth.provider;

import io.sodabox.mod.http.oauth.OAuthConfig;
import io.sodabox.mod.http.oauth.Profile;
import io.sodabox.mod.http.oauth.exception.UserDeniedPermissionException;
import io.sodabox.mod.http.oauth.strategy.Hybrid;
import io.sodabox.mod.http.oauth.strategy.OAuthStrategyBase;
import io.sodabox.mod.http.oauth.strategy.RequestToken;
import io.sodabox.mod.http.oauth.utils.AccessGrant;
import io.sodabox.mod.http.oauth.utils.Constants;
import io.sodabox.mod.http.oauth.utils.OpenIdConsumer;

import java.util.HashMap;
import java.util.Map;

public class GoogleImpl extends AbstractProvider implements AuthProvider {

	private static final String OAUTH_SCOPE = "http://www.google.com/m8/feeds/";
	private static final Map<String, String> ENDPOINTS;

	private OAuthConfig config;
	private OAuthStrategyBase authenticationStrategy;

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
				"https://www.google.com/accounts/o8/ud");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://www.google.com/accounts/OAuthGetAccessToken");
	}

	public GoogleImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
		authenticationStrategy = new Hybrid(config, ENDPOINTS);
		authenticationStrategy.setScope(getScope());
	}

	@Override
	public RequestToken getLoginRedirectURL() throws Exception {
		return authenticationStrategy.getLoginRedirectURL();
	}

	@Override
	public Profile connect(AccessGrant requestToken, final Map<String, String> requestParams)
			throws Exception {
		if (requestParams.get("openid.mode") != null
				&& "cancel".equals(requestParams.get("openid.mode"))) {
			throw new UserDeniedPermissionException();
		}
		AccessGrant accessToken = authenticationStrategy.verifyResponse(requestToken, requestParams);
		return getProfile(accessToken, requestParams);
	}

	private Profile getProfile(final AccessGrant accessToken, final Map<String, String> requestParams) {
		Profile userProfile = OpenIdConsumer.getUserInfo(requestParams);
		//userProfile.setProviderId(getProviderId());
		System.out.println("User Info : " + userProfile.toString());
		return userProfile;
	}

	@Override
	public String getProviderId() {
		return config.getId();
	}


	private String getScope() {
		return OAUTH_SCOPE;
	}
}
