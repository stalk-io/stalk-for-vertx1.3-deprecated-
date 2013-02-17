package io.stalk.common.api;

import org.vertx.java.core.json.JsonObject;

public class MailSenderConfig {

	private String 	address;
	private boolean ssl;
	private String 	host;
	private int 	port;
	private boolean auth;
	private String 	username;
	private String 	password;
	private String 	contentType;
	private String 	fromEmail;

	private String 	title;
	private String 	content;

	public MailSenderConfig(JsonObject json) {
		this.address 	= json.getString("address"		, "mailSender");
		this.ssl 		= json.getBoolean("ssl"			, false);
		this.host 		= json.getString("host"			, "localhost");
		this.port 		= json.getNumber("port"			, 25).intValue();
		this.auth 		= json.getBoolean("auth"		, false);
		this.username 	= json.getString("username"		, null);
		this.password 	= json.getString("password"		, null);
		this.contentType= json.getString("content_type"	, "text/plain");
		this.fromEmail	= json.getString("from-email"	, "stalk.io.reply@gmail.com");

		this.title		= json.getString("title"		, "Activation for your own chat service from stalk.io");
		this.content	= json.getString("content"		, 
				"This is an automated email sent by the <b>stalk.io</b> service upon request<br>"+
						"from the <b>stalk.io</b> administrator.<br><br>"+
						"In order to activate your <b>stalk.io</b> account, please click "+
						"<a href='http://stalk.io/user/auth/#LINK_PATH#' target='_blank'>link</a><br>"+
						"or <a href='http://stalk.io/user/auth/#LINK_PATH#' target='_blank'>http://stalk.io/user/auth/#LINK_PATH#</a><br><br>"+
						"Enjoy your own chat service!!"
				);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	

}
