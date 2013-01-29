package io.sodabox.mod.http;

import io.sodabox.common.api.PUBLISH_MANAGER;
import io.sodabox.common.api.SESSION_MANAGER;
import io.sodabox.mod.http.oauth.Profile;
import io.sodabox.mod.http.oauth.strategy.RequestToken;
import io.sodabox.mod.http.oauth.utils.AccessGrant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class WebServer extends AbstractModule{

	interface OAUTH_COOKIE {
		String NAME 	= "SODABOX";

		interface VALUE{
			String CHANNEL 			= "_channel";
			String SOCKET_ID 		= "_socketId";
			String REFER 			= "_refer";
			String TARGET 			= "_target";
			String REQUEST_TOKEN 	= "_requestToken";
		}
	}

	@Override
	public void handle(final HttpServerRequest req) {

		req.bodyHandler(new Handler<Buffer>(){
			public void handle(Buffer arg0) {
				System.out.println(arg0);
			};
		});

		// server node 받아오기.
		if("/node".equals(req.path)){

			if(!StringUtils.isEmpty(req.params().get("refer"))){

				JsonObject reqJson = new JsonObject();
				reqJson.putString("action"	, SESSION_MANAGER.ACTION.IN);
				reqJson.putString("refer"	, req.params().get("refer"));
				
				eb.send(SESSION_MANAGER.ADDRESS, reqJson, new Handler<Message<JsonObject>>() {
					public void handle(Message<JsonObject> message) {

						StringBuffer returnStr = new StringBuffer("");
						if(StringUtils.isEmpty(req.params().get("callback"))){
							returnStr.append(message.body.encode());
						}else{
							returnStr
							.append(req.params().get("callback"))
							.append("(")
							.append(message.body.encode())
							.append(");");
						}

						req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE	, "application/json; charset=UTF-8");
						req.response.end(returnStr.toString());    

					}
				});

			}else{
				req.response.end("");    
			}

		}else if("/auth".equals(req.path)){ // 인증 시

			String target = req.params().get("target");

			try {

				RequestToken requestToken = authManager.getAuthenticationUrl(target);

				JsonObject cookieJsonObject = new JsonObject();
				cookieJsonObject.putString(OAUTH_COOKIE.VALUE.CHANNEL	, req.params().get("channel")	);
				cookieJsonObject.putString(OAUTH_COOKIE.VALUE.SOCKET_ID	, req.params().get("socketId")	);
				cookieJsonObject.putString(OAUTH_COOKIE.VALUE.REFER		, req.params().get("refer")		);
				cookieJsonObject.putString(OAUTH_COOKIE.VALUE.TARGET	, req.params().get("target")	);
				if(requestToken.getAccessGrant() != null) {
					cookieJsonObject.putString(OAUTH_COOKIE.VALUE.REQUEST_TOKEN, getJsonObjectFromAccessGrant(requestToken.getAccessGrant()).encode());
				}

				// Set Cookies
				CookieEncoder httpCookieEncoder = new CookieEncoder(true);	
				httpCookieEncoder.addCookie(OAUTH_COOKIE.NAME				, cookieJsonObject.encode());
				req.response.headers().put(HttpHeaders.Names.SET_COOKIE		, httpCookieEncoder.encode());

				req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE	, "text/html; charset=UTF-8");
				req.response.end("<script type='text/javascript'>location.href='"+requestToken.getUrl()+"';</script>");

			} catch (Exception e) {
				e.printStackTrace();

				// Delete Cookies
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				req.response.headers().put(HttpHeaders.Names.SET_COOKIE		, cookieEncoder.encode());
				req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE	, "text/html; charset=UTF-8");
				req.response.end("<html><body><h1>^^</h1><br>"+e.getMessage()+"</body></html>");
			}



		}else if("/auth/callback".equals(req.path)){ // 인증 완료.

			String value = req.headers().get(HttpHeaders.Names.COOKIE);
			DEBUG("cookie string : %s ", value);
			Set<Cookie> cookies = new CookieDecoder().decode(value);

			String refer 		= "";
			String channel 		= "";
			String socketId 	= "";
			String target	 	= "";

			AccessGrant accessToken = null;
			for (Cookie cookie : cookies) {
				DEBUG("cookies : %s", cookie.toString());
				if (cookie.getName().equals(OAUTH_COOKIE.NAME)) {
					JsonObject cookieJson = new JsonObject(cookie.getValue());

					DEBUG("SODABOX cookies : %s", cookieJson.encode());

					JsonObject json = new JsonObject(cookieJson.getString(OAUTH_COOKIE.VALUE.REQUEST_TOKEN));

					if(json != null){
						accessToken = new AccessGrant();
						accessToken.setKey(json.getString("key"));
						accessToken.setSecret(json.getString("secret"));
						accessToken.setProviderId(json.getString("providerId"));
						JsonObject attrJson = json.getObject("attributes");

						if(attrJson != null){
							Map<String, Object> attributes = attrJson.toMap();
							accessToken.setAttributes(attributes);
						}
					}

					channel 	= cookieJson.getString(OAUTH_COOKIE.VALUE.CHANNEL	);
					socketId 	= cookieJson.getString(OAUTH_COOKIE.VALUE.SOCKET_ID	);
					refer 		= cookieJson.getString(OAUTH_COOKIE.VALUE.REFER		);
					target 		= cookieJson.getString(OAUTH_COOKIE.VALUE.TARGET	);

					break;
				}
			}

			if(target != null){
				try {

					Profile user = authManager.connect(target, req.params(), accessToken);

					DEBUG("OAUTH Profile : %s ", user.toString());

					JsonObject profileJson = new JsonObject();
					profileJson.putString("name"	, user.getName());
					profileJson.putString("link"	, user.getLink());
					profileJson.putString("target"	, target);

					// @ TODO more !!

					JsonObject jsonMessage = new JsonObject();
					jsonMessage.putString("action"		, PUBLISH_MANAGER.ACTION.PUB);
					jsonMessage.putString("type"		, "LOGIN");
					jsonMessage.putString("channel"		, channel);
					jsonMessage.putString("socketId"	, socketId);
					jsonMessage.putString("refer"		, refer);
					jsonMessage.putObject("user"		, profileJson);

					eb.send(PUBLISH_MANAGER.ADDRESS, jsonMessage);


					// Delete Cookies
					CookieEncoder cookieEncoder = new CookieEncoder(true);
					req.response.headers().put(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());

					req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
					req.response.end("<script type='text/javascript'>window.close();</script>");

				} catch (Exception e) {
					e.printStackTrace();
					req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
					req.response.end("<html><body><h1>^^</h1><br>"+e.getMessage()+"</body></html>");
				}
			}else{

				// Delete Cookies
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				req.response.headers().put(HttpHeaders.Names.SET_COOKIE		, cookieEncoder.encode());

				req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE	, "text/html; charset=UTF-8");
				req.response.end("<html><body><h1>^^</h1><br> Denied."+cookieEncoder.encode()+"</body></html>");

			}

		}else if("/preview".equals(req.path)){

			String addHtml = 
					"<script type=\"text/javascript\" src=\"http://www.sodabox.io/sodabox.js\" charset=\"utf-8\"></script><script language=\"javascript\">SODABOX.init();</script>";
			StringBuffer html = new StringBuffer();

			String result = "";
			if(!StringUtils.isEmpty(req.params().get("url"))){
				try {

					URL uu = new URL(req.params().get("url"));
					BufferedReader in = new BufferedReader(
							new InputStreamReader(uu.openStream(), "UTF-8"));

					String inputLine;
					while ((inputLine = in.readLine()) != null)
						html.append(inputLine);
					in.close();

					
					String baseHref = "<base href=\""+req.params().get("url")+"\">";
					
					result = 
							html.substring(0, html.indexOf("</head>")) +
							baseHref +
							html.substring(html.indexOf("</head>"), html.indexOf("</body>")) +
							addHtml +
							html.substring(html.indexOf("</body>"));

				} catch (IOException e) {
					e.printStackTrace();
					result = e.getMessage();
				}
			}else{
				result = "url is not valid";
			}

			req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE	, "text/html");
			req.response.end(result);

		}
		else{
			localResponse(req);
		}

	}

	private JsonObject getJsonObjectFromAccessGrant(AccessGrant accessGrant) {

		JsonObject json = new JsonObject();
		json.putString("key", accessGrant.getKey());
		json.putString("secret", accessGrant.getSecret());
		json.putString("providerId", accessGrant.getProviderId());

		Map<String, Object> attributes = accessGrant.getAttributes();
		if(attributes != null){
			JsonObject jsonAttr = new JsonObject();
			for (String key : attributes.keySet()) {
				jsonAttr.putString(key, attributes.get(key).toString());
			}
			json.putObject("attributes", jsonAttr);
		}
		return json;

	}

}