package io.sodabox.mod;


import io.sodabox.common.api.NODE_WATCHER;
import io.sodabox.common.server.zk.ZooKeeperClient;
import io.sodabox.common.server.zk.ZooKeeperClient.Credentials;
import io.sodabox.common.server.zk.ZooKeeperConnectionException;
import io.sodabox.common.server.zk.ZooKeeperUtils;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

public class NodeWatcher extends BusModBase implements Handler<Message<JsonObject>> {


	private Logger log;

	private ZooKeeperClient zkClient;
	private String 			address;
	private String 			rootPath;
	private boolean 		isReady;
	private boolean 		isWatching;
	private boolean			isCreated;

	public void start() {

		super.start();

		log 							= container.getLogger();
		address 						= getOptionalStringConfig(	NODE_WATCHER.ADDRESS	, NODE_WATCHER.DEFAULT.ADDRESS);
		rootPath						= getOptionalStringConfig(	NODE_WATCHER.ROOT_PATH	, NODE_WATCHER.DEFAULT.ROOT_PATH);

		JsonArray 	zookeeperServers 	= getOptionalArrayConfig(NODE_WATCHER.ZOOKEEPER_SERVERS, null);
		int 		zookeeperTomeout 	= getOptionalIntConfig(		NODE_WATCHER.TIMEOUT	, NODE_WATCHER.DEFAULT.TIMEOUT);

		/* connect zookeeper servers */
		try {

			zkClient = new ZooKeeperClient(zookeeperTomeout, Credentials.NONE, zookeeperServers);
			isReady = true;
		} catch (ZooKeeperConnectionException e) {
			ERROR("zookeeper is not existed [%s]", zookeeperServers.encode());
			isReady = false;
			e.printStackTrace();
		}

		eb.registerHandler(address, this);
	}

	@Override
	public void stop() {
		try {
			super.stop();
			zkClient.close();
		} catch (Exception e) {
		}
	}

	@Override
	public void handle(Message<JsonObject> message) {
		String action = message.body.getString("action");

		if(isReady){

			if(NODE_WATCHER.ACTION.CREATE_NODE.equals(action)){
				
				if(isCreated){
					
					sendOK(message);
					
				}else{
				
					try {
						createNode(
								message.body.getString("channel"),
								message.body.getObject("data")
								);
	
						isCreated = true;
						
						// OK 
						sendOK(message);
	
					} catch (ZooKeeperConnectionException 
							| InterruptedException
							| KeeperException e) {
						e.printStackTrace();
	
						// ERROR
						sendError(message, e.getMessage());
	
					}
				}

			}else if(NODE_WATCHER.ACTION.START_WATCHING.equals(action)){

				if(!isWatching){
					watching();
					isWatching = false;
				}
				sendOK(message);
			}

		}else{
			sendError(message, "zookeeper is not ready");
		}

	}

	private void watching(){
		/* 2. watching the children nodes */
		try {

			ZooKeeperUtils.ensurePath(zkClient, ZooDefs.Ids.OPEN_ACL_UNSAFE, rootPath);

			List<String> channels = zkClient.get().getChildren(rootPath, new Watcher() {
				public void process(WatchedEvent event) {
					try {
						List<String> channels = zkClient.get().getChildren(rootPath, this);
						DEBUG("** WATCHED ** %s %s", rootPath, channels);
						refreshNode(channels);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});

			if(channels.size() > 0){

				refreshNode(channels);

			}else{

				ERROR(" message server is not existed from [%s]..", rootPath);

			}

		} catch (KeeperException | InterruptedException
				| ZooKeeperConnectionException e) {
			e.printStackTrace();
			ERROR("%s", e.getMessage());
		}
	}

	private void createNode(final String channel, final JsonObject data) throws ZooKeeperConnectionException, InterruptedException, KeeperException{

		ZooKeeperUtils.ensurePath(zkClient, ZooDefs.Ids.OPEN_ACL_UNSAFE, rootPath);

		if (zkClient.get().exists(rootPath+"/"+channel, false) == null) {

			DEBUG("create node [%s]", data.encode());

			zkClient.get().create(
					rootPath+"/"+channel, 
					data.encode().getBytes(), 
					ZooDefs.Ids.OPEN_ACL_UNSAFE, 
					CreateMode.EPHEMERAL);
		}

	}

	private void refreshNode(List<String> channels) throws KeeperException, InterruptedException, ZooKeeperConnectionException{

		if(channels.size() > 0){

			JsonArray servers = new JsonArray();
			JsonArray redises = new JsonArray();

			for(String channel : channels){

				JsonObject nodes = new JsonObject(
						new String(zkClient.get().getData(rootPath + "/"+channel, false, null))
						);

				servers.addObject(nodes.getObject("server").putString("channel", channel));
				redises.addObject(nodes.getObject("redis").putString("channel", channel));

			}

			eb.publish("sessionManager", 
					new JsonObject().putString("action", "refresh").putArray("channels", servers) );

			eb.publish("nodeManager", 
					new JsonObject().putString("action", "refresh").putArray("channels", redises) );

		}else{

			ERROR("message server is not existed from [%s]", rootPath);

			eb.publish("sessionManager", 
					new JsonObject().putString("action", "destroy"));

			eb.publish("nodeManager", 
					new JsonObject().putString("action", "destroy"));

		}

	}

	protected void DEBUG(String message, Object... args ){
		if(log != null) log.debug("[MOD::NODE] "+String.format(message, args));
	}

	protected void ERROR(String message, Object... args ){
		if(log != null) log.error("[MOD::NODE] "+String.format(message, args));
	}

}
