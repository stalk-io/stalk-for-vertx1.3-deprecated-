package io.sodabox.mod;


import io.sodabox.common.api.PUBLISH_MANAGER;
import io.sodabox.common.server.NodeManager;
import io.sodabox.common.server.RedisNodeManager;
import io.sodabox.common.server.node.RedisPoolNode;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

public class PublishManager extends BusModBase implements Handler<Message<JsonObject>> {

	private Logger log;
	private String address;
	private boolean isReady;

	private NodeManager<RedisPoolNode> 	redisNodeManager;

	public void start() {

		super.start();

		log = container.getLogger();
		address =  getOptionalStringConfig(PUBLISH_MANAGER.ADDRESS	, PUBLISH_MANAGER.DEFAULT.ADDRESS);

		if(log.isDebugEnabled()){
			redisNodeManager = new RedisNodeManager(log);
		}else{
			redisNodeManager = new RedisNodeManager();
		}

		isReady = true;

		eb.registerHandler(address, this);
	}

	protected void DEBUG(String message, Object... args ){
		if(log != null) log.debug("[MOD::NODE] "+String.format(message, args));
	}
	protected void ERROR(String message, Object... args ){
		if(log != null) log.error("[MOD::NODE] "+String.format(message, args));
	}


	@Override
	public void stop() {
		try {
			super.stop();
			if(redisNodeManager != null) 	redisNodeManager.destoryNode();
		} catch (Exception e) {
		}
	}

	@Override
	public void handle(Message<JsonObject> message) {
		String action = message.body.getString("action");

		if(isReady){

			if(PUBLISH_MANAGER.ACTION.REFRESH_NODES.equals(action)){
				JsonArray channels = message.body.getArray("channels");

				if(channels != null){
					redisNodeManager.refreshNode(channels);
				}

			}else if(PUBLISH_MANAGER.ACTION.DESTORY_NODES.equals(action)){
				redisNodeManager.destoryNode();

			}else if(PUBLISH_MANAGER.ACTION.PUB.equals(action)){

				String channel = message.body.getString("channel");
				RedisPoolNode redisNode = redisNodeManager.getNode(channel);
				long result = redisNode.publish(channel, message.body.encode());

				JsonObject json = new JsonObject();
				json.putNumber("result", 	result);

				sendOK(message, json);
			}

		}else{
			sendError(message, "message server is not existed");
		}

	}

}
