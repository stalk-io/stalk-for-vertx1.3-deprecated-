package io.sodabox.mod.http.oauth.strategy;

import io.sodabox.mod.http.oauth.utils.AccessGrant;

public class RequestToken {

	private String url;
	private AccessGrant accessGrant;
	
	public RequestToken(String url, AccessGrant accessGrant) {
		this.url = url;
		this.accessGrant = accessGrant;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public AccessGrant getAccessGrant() {
		return accessGrant;
	}
	public void setAccessGrant(AccessGrant accessGrant) {
		this.accessGrant = accessGrant;
	}
	
	
	
}
