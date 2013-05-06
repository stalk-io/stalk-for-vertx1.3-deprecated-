package io.stalk.mod.socket;

import io.stalk.common.api.NODE_WATCHER;
import io.stalk.common.api.SOCKET_SERVER;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.sockjs.SockJSSocket;

public abstract class AbstractModule extends BusModBase implements Handler<SockJSSocket>{

	private Logger log;

	protected String channel;
	protected String address;

	protected boolean isOk = false;

	// socketId : refer^user
	protected ConcurrentMap<String, String> sessionStore;


	@Override
	public void start() {

		super.start();

		log = container.getLogger();
		sessionStore = vertx.sharedData().getMap("_REFER_STORAGE");

		address = getOptionalStringConfig(SOCKET_SERVER.ADDRESS, SOCKET_SERVER.DEFAULT.ADDRESS);
		channel = getMandatoryStringConfig("channel");

		HttpServer server = vertx.createHttpServer();

		server.requestHandler(new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {

				if(req.path.startsWith("/status")){
					processRequest(req);
				}else if (!req.path.startsWith("/message")){
					System.out.println(" *** bad request path *** "+req.path);
					req.response.statusCode = 404;
					req.response.end();
				}
			}
		});

		JsonObject serverConf 	= getOptionalObjectConfig("server"	, new JsonObject());
		JsonObject redisConf 	= getOptionalObjectConfig("redis"	, new JsonObject());

		String 	host = serverConf.getString(	SOCKET_SERVER.HOST);
		int		port = serverConf.getInteger(	SOCKET_SERVER.PORT);

		SockJSServer sockServer = vertx.createSockJSServer(server);
		sockServer.installApp(new JsonObject().putString("prefix", "/message"), this);
		server.listen(port); //, host);

		// create server node!!
		JsonObject createNodeAction = new JsonObject();
		createNodeAction.putString("action", NODE_WATCHER.ACTION.CREATE_NODE);
		createNodeAction.putString("channel", channel);
		createNodeAction.putObject("data", 
				new JsonObject()
		.putObject("server"	, serverConf)
		.putObject("redis"	, redisConf	)
				);

		eb.send(NODE_WATCHER.DEFAULT.ADDRESS, createNodeAction);

		// starting watching nodes !!!!
		eb.send(NODE_WATCHER.DEFAULT.ADDRESS, new JsonObject().putString("action", NODE_WATCHER.ACTION.START_WATCHING));

		eb.registerHandler(address, getMessageHandler());

		DEBUG("Message Server(%s) is started [%s:%d]", address, host, port);
	}
	
	@Override
	public void stop() {
		try {
			super.stop();
			
			JsonObject delNode = new JsonObject();
			delNode.putString("action", NODE_WATCHER.ACTION.DEL_NODE);
			delNode.putString("channel", channel);
			eb.send(NODE_WATCHER.DEFAULT.ADDRESS, delNode);

		} catch (Exception e) {
		}
	}

	protected abstract Handler<Message<JsonObject>> getMessageHandler();
	protected abstract void processRequest(HttpServerRequest req);

	protected String getRefer(String str){
		if(str.indexOf("https://") >= 0){
			if(str.substring(8).indexOf('/') >= 0){
				return str.substring(8, 8+str.substring(8).indexOf('/'));
			}else{
				return str;
			}
		} else if(str.indexOf("http://") >= 0){
			if(str.substring(7).indexOf('/') >= 0){
				return str.substring(7, 7+str.substring(7).indexOf('/'));
			}else{
				return str;
			}
		} else {
			if(str.indexOf('/') >= 0){
				return str.substring(0, str.indexOf('/'));
			}else{
				return str;
			}
		}
	}

	protected Session getSessionInfo(String socketId){
		String str = sessionStore.get(socketId);
		return new Session(str);
	}
	protected void removeSessionInfo(String socketId){
		sessionStore.remove(socketId);
	}
	protected void addSessionInfo(String socketId, String refer, String user){
		sessionStore.put(socketId, refer+"^"+user);
	}


	protected void addSocketId(String refer, String socketId){
		vertx.sharedData().getSet(refer).add(socketId);
	}
	protected Set<String> getSocketIds(String refer){
		return vertx.sharedData().getSet(refer);
	}
	protected void removeSocketId(String refer, String socketId){
		vertx.sharedData().getSet(refer).remove(socketId);
	}
	protected int getSocketsCount(String refer){
		return vertx.sharedData().getSet(refer).size();
	}

	protected void sendMessage(String socketId, String message){

		DEBUG("SEND MESSAGE %s -> %s", socketId, message);

		vertx.eventBus().send(socketId, new Buffer(message));
	}

	protected void sendMessageToAll(String refer, String message){
		Set<String> socks = getSocketIds(refer);
		for(String socketId : socks){
			sendMessage(socketId, message);
		}
	}

	class Session {

		public String REFER;
		public String USER;

		public Session(String data) {
			REFER = data.substring(0, data.indexOf("^"));
			USER  = data.substring(data.indexOf("^")+1);
		}
	}

	protected void DEBUG(String message, Object... args ){
		if(log != null) log.debug(String.format(message, args));
	}
	protected void ERROR(String message, Object... args ){
		if(log != null) log.debug(String.format(message, args));
	}

}
