package io.sodabox.mod.message;

import io.sodabox.common.api.SESSION_MANAGER;
import io.sodabox.common.api.SOCKET_SERVER;

import java.util.Set;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSSocket;

public class SockServer extends AbstractModule {

	public void handle(final SockJSSocket sock) {

		sock.endHandler(new Handler<Void>(){
			public void handle(Void event) {

				String socketId = sock.writeHandlerID;

				Session session = getSessionInfo(socketId);

				removeSocketId(session.REFER, socketId);

				DEBUG(" ** OUT ** %s / %s ", session.REFER, session.USER);

				int cnt = getSocketsCount(session.REFER);
				if(cnt > 0){

					JsonObject json = new JsonObject();
					json.putString("action", "OUT");
					json.putObject("user", new JsonObject(session.USER));
					json.putNumber("count", cnt);

					sendMessageToAll(session.REFER, json.encode());	
				}

				eb.send(SESSION_MANAGER.ADDRESS, 
						new JsonObject()
				.putString("action", SESSION_MANAGER.ACTION.UPDATE)
				.putString("refer", session.REFER)
				.putNumber("count", cnt)
				.putString("channel", channel)
						);

				removeSessionInfo(socketId);

			}
		});

		sock.dataHandler(new Handler<Buffer>() {
			public void handle(Buffer data) {

				JsonObject reqJson = new JsonObject(data.toString());

				if( "JOIN".equals(reqJson.getString("action")) ){

					DEBUG("[[JOIN]] : %s",reqJson.encode());

					String 		refer 		= reqJson.getString("refer");
					JsonObject 	user 		= reqJson.getObject("user", new JsonObject());
					String 		socketId 	= sock.writeHandlerID;

					// add socketId for refer 
					addSocketId(refer, socketId);

					// add session info
					String userStr = user.encode();
					addSessionInfo(socketId, refer, userStr);

					Set<String> socks = getSocketIds(refer);

					eb.send(SESSION_MANAGER.ADDRESS, 
							new JsonObject()
					.putString("action", SESSION_MANAGER.ACTION.UPDATE)
					.putString("refer", refer)
					.putNumber("count", socks.size())
					.putString("channel", channel)
							);

					for(String target : socks){

						if(target.equals(socketId)){ // ME !!

							JsonObject json = new JsonObject();
							json.putString("action"		, "JOIN");
							json.putNumber("count"		, socks.size());
							json.putString("socketId"	, socketId);
							sendMessage(target, json.encode());

						}else{

							JsonObject json = new JsonObject();
							json.putString("action"		, "IN");
							json.putNumber("count"		, socks.size());
							json.putString("user"		, userStr);
							sendMessage(target, json.encode());	
						}

					}

				}else if( "MESSAGE".equals(reqJson.getString("action")) ){

					JsonObject json = new JsonObject();
					json.putString("action"		, "MESSAGE");
					json.putString("message"	, reqJson.getString("message"));
					json.putObject("user"		, reqJson.getObject("user"));

					sendMessageToAll(reqJson.getString("refer"), json.encode());

				}else if( "LOGOUT".equals(reqJson.getString("action")) ){
					String socketId = sock.writeHandlerID;

					Session session = getSessionInfo(socketId);

					DEBUG(" ** LOGOUT.. ** %s / %s ", session.REFER, session.USER);

					int cnt = getSocketsCount(session.REFER);

					if(cnt > 0){

						JsonObject json = new JsonObject();
						json.putString("action"	, "LOGOUT");
						json.putObject("user"	, new JsonObject(session.USER));
						json.putNumber("count"	, cnt);

						sendMessageToAll(session.REFER, json.encode());	
					}

					removeSocketId(session.REFER, socketId);
					removeSessionInfo(socketId);

				}else{
					sock.writeBuffer(data);
				}

			}
		});		
	}

	@Override
	protected Handler<Message<JsonObject>> getMessageHandler(){

		return new Handler<Message<JsonObject>>() {
			public void handle(Message<JsonObject> message) {

				String action 	= message.body.getString("action");
				String socketId = message.body.getString("socketId");

				if(SOCKET_SERVER.ACTION.MESSAGE.equals(action)){

					message.body.putString("action", "LOGIN");
					sendMessage(socketId, message.body.encode());

				}
			}

		};
	}


}
