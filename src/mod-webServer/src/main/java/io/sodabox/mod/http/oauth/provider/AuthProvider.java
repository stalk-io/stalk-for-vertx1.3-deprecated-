package io.sodabox.mod.http.oauth.provider;

import io.sodabox.mod.http.oauth.Profile;
import io.sodabox.mod.http.oauth.strategy.RequestToken;
import io.sodabox.mod.http.oauth.utils.AccessGrant;

import java.util.Map;


public interface AuthProvider {

	String EXT_NAMESPACE = "http://specs.openid.net/extensions/oauth/1.0";
	String EMAIL = "email";
	String COUNTRY = "country";
	String LANGUAGE = "language";
	String FULL_NAME = "fullname";
	String NICK_NAME = "nickname";
	String DOB = "dob";
	String GENDER = "gender";
	String POSTCODE = "postcode";
	String FIRST_NAME = "firstname";
	String LAST_NAME = "lastname";

	public RequestToken getLoginRedirectURL() throws Exception;

	public Profile connect(AccessGrant requestToken, Map<String, String> requestParams) throws Exception;

	public String getProviderId();

}
