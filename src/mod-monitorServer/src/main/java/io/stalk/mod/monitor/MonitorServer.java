package io.stalk.mod.monitor;

import io.stalk.common.api.MONITOR_SERVER;
import io.stalk.common.api.SESSION_MONITOR;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

public class MonitorServer extends BusModBase implements Handler<HttpServerRequest> {

	protected Logger 	log;

	protected String 	address;

	public void start() {
		super.start();

		log = container.getLogger();

		address 		= getOptionalStringConfig	(MONITOR_SERVER.ADDRESS, 	MONITOR_SERVER.DEFAULT.ADDRESS);
		String host		= getOptionalStringConfig	(MONITOR_SERVER.HOST, 		MONITOR_SERVER.DEFAULT.HOST);
		int	   port		= getOptionalIntConfig		(MONITOR_SERVER.PORT,		MONITOR_SERVER.DEFAULT.PORT);

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(this);
		server.listen(port, host);

		DEBUG("Web Server is started [%s:%d]", host, port);

	}

	public void handle(final HttpServerRequest req) {

		if("/list".equals(req.path)){

			JsonObject reqJson = new JsonObject();
			reqJson.putString("action"	, SESSION_MONITOR.ACTION.LIST);
			reqJson.putString("site"	, req.params().get("site"));

			eb.send(SESSION_MONITOR.DEFAULT.ADDRESS, reqJson, new Handler<Message<JsonObject>>() {
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
		}else if("/info".equals(req.path)){

			JsonObject reqJson = new JsonObject();
			reqJson.putString("action"	, SESSION_MONITOR.ACTION.INFO);

			eb.send(SESSION_MONITOR.DEFAULT.ADDRESS, reqJson, new Handler<Message<JsonObject>>() {
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
			req.response.headers().put(HttpHeaders.Names.CONTENT_TYPE	, "text/html; charset=UTF-8");
			req.response.end("<script type='text/javascript'>location.href='http://stalk.io';</script>");
		}
	}


	protected void DEBUG(String message, Object... args ){
		if(log != null) log.debug(String.format(message, args));
	}
	protected void ERROR(String message, Object... args ){
		if(log != null) log.debug(String.format(message, args));
	}


}
