package io.sodabox.mod;

import io.sodabox.common.api.SESSION_MANAGER;
import io.sodabox.common.server.NodeManager;
import io.sodabox.common.server.ServerNodeManager;
import io.sodabox.common.server.node.ServerNode;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SessionManager extends BusModBase implements Handler<Message<JsonObject>> {

	private Logger 			log;

	private String 			address;
	private boolean 		isReady;

	private JedisPool 		redisPool;

	private NodeManager<ServerNode> 	serverNodeManager;

	public void start() {

		super.start();

		log = container.getLogger();
		address = getOptionalStringConfig(SESSION_MANAGER.ADDRESS	, SESSION_MANAGER.DEFAULT.ADDRESS);

		if(log.isDebugEnabled()){
			serverNodeManager = new ServerNodeManager(log);
		}else{
			serverNodeManager = new ServerNodeManager();
		}

		JsonObject 	sessionConf = getOptionalObjectConfig(SESSION_MANAGER.SESSION_STORAGE, null);
		DEBUG("session config - %s", sessionConf);
		
		if(sessionConf != null){

			JedisPoolConfig config = new JedisPoolConfig();
			config.testOnBorrow = true;

			String host = sessionConf.getString("host");
			int port 	= sessionConf.getInteger("port").intValue();
			
			JedisPool jedisPool;
			if( StringUtils.isEmpty(host) ){
				jedisPool = new JedisPool(config, "localhost");
			}else{
				jedisPool = new JedisPool(config, host, port);
			}
			DEBUG("session storage CONNECTED - %s:%d", host, port);
			
			this.redisPool = jedisPool;
		}

		eb.registerHandler(address, this);
	}

	@Override
	public void stop() {
		try {
			super.stop();
			this.redisPool.destroy();
			if(serverNodeManager != null) 	serverNodeManager.destoryNode();
		} catch (Exception e) {
		}
	}

	@Override
	public void handle(final Message<JsonObject> message) {
		String action = message.body.getString("action");

		if(isReady){

			ServerNode serverNode = null;

			if(SESSION_MANAGER.ACTION.IN.equals(action)){ // "in"

				if(this.redisPool == null){

					String refer = message.body.getString("refer");
					serverNode = serverNodeManager.getNode(refer);

				}else{

					Jedis jedis 	= this.redisPool.getResource();

					try {
						String refer = message.body.getString("refer");

						String[] refers = getHostUrl(refer);

						String key 		= refers[0];
						String field 	= refers[1];

						String channelAndCount = jedis.hget(key, field);

						// not existed in session redis.
						if(channelAndCount == null){
							
							serverNode = serverNodeManager.getNode(refer);

							DEBUG(" ACTION[in] (Not Existed in Session Storage) - %s", serverNode.getChannel());
							
							jedis.hset(key, field, serverNode.getChannel()+"^0"); // init channel and count '0'

						}else{

							String 	channel	= channelAndCount.substring(0, channelAndCount.indexOf("^"));
							//int 	count  	= Integer.parseInt(channelAndCount.substring(channelAndCount.indexOf("^")+1));

							serverNode = serverNodeManager.getNodeByKey(channel);
							
							if(serverNode == null){ // when the target server is crushed!!

								// delete session info.
								jedis.hdel(key, field);

								DEBUG(" ACTION[in] (CRUSHED!!! retry to get new Node)");
								
								serverNode = serverNodeManager.getNode(refer);
								if(serverNode != null) jedis.hset(key, field, serverNode.getChannel()+"^0"); //channel and count '0'

							}

							DEBUG(" ACTION[in] (Existed) - %s", serverNode.getChannel());
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						this.redisPool.returnResource(jedis);
					}

					if(serverNode != null){
						JsonObject json = new JsonObject();
						json.putString("channel", 	serverNode.getChannel());
						json.putString("host", 		serverNode.getHost());
						json.putNumber("port", 		serverNode.getPort());

						sendOK(message, json);
					}else{
						sendError(message, "server node is not existed.");
					}

				}

			}else if(SESSION_MANAGER.ACTION.UPDATE.equals(action)){

				String refer = message.body.getString("refer");
				int count = message.body.getInteger("count").intValue();

				String[] refers = getHostUrl(refer);

				Jedis jedis = this.redisPool.getResource();

				try {
					if(count > 0){

						String channel = message.body.getString("channel");
						jedis.hset(refers[0], refers[1], channel+"^"+count);

					}else{

						jedis.hdel(refers[0], refers[1]);

					}
				} catch (Exception e) {
					e.printStackTrace();

				} finally {
					this.redisPool.returnResource(jedis);
				}

				sendOK(message);

			}else if(SESSION_MANAGER.ACTION.LIST.equals(action)){

				String refer = message.body.getString("refer");

				String[] refers = getHostUrl(refer);

				Jedis jedis = this.redisPool.getResource();
				Map<String, String> list = jedis.hgetAll(refers[0]);
				this.redisPool.returnResource(jedis);

				JsonArray paths = new JsonArray();
				for (Map.Entry<String, String> entry: list.entrySet()) {

					String value = entry.getValue();

					JsonObject obj = new JsonObject();
					obj.putString("path", entry.getKey());
					obj.putNumber("cnt", Integer.parseInt(value.substring(value.indexOf("^")+1)));
					paths.addObject(obj);
				}

				JsonObject listData = new JsonObject();
				listData.putString("url", refers[0]);
				listData.putNumber("size", list.size());
				listData.putArray("paths", paths);

				sendOK(message, listData);

			}else if(SESSION_MANAGER.ACTION.REFRESH_NODES.equals(action)){
				JsonArray channels = message.body.getArray("channels");
				if(channels != null){
					serverNodeManager.refreshNode(channels);
				}

			}else if(SESSION_MANAGER.ACTION.DESTORY_NODES.equals(action)){
				serverNodeManager.destoryNode();
			}

		}else{
			sendError(message, "session storage is not existed");
		}

	}

	private String[] getHostUrl(String str){

		String[] rtn = new String[2];

		if(str.indexOf("https://") >= 0){
			if(str.substring(8).indexOf("/") >= 0){
				int s =  8+str.substring(8).indexOf("/");
				rtn[0] = str.substring(0, s);
				rtn[1] = str.substring(s);
			}else{
				rtn[0] =  str;
				rtn[1] = "/";
			}
		} else if(str.indexOf("http://") >= 0){
			if(str.substring(7).indexOf("/") >= 0){
				int s =  7+str.substring(7).indexOf("/");
				rtn[0] = str.substring(0, s);
				rtn[1] = str.substring(s);
			}else{
				rtn[0] = str;
				rtn[1] = "/";
			}
		} else {
			if(str.indexOf("/") >= 0){
				rtn[0] = str.substring(0, str.indexOf("/"));
				rtn[1] = str.substring(str.indexOf("/"));
			}else{
				rtn[0] = str;
				rtn[1] = "/";
			}
		}

		return rtn;

	}

	protected void DEBUG(String message, Object... args ){
		if(log != null) log.debug("[MOD::NODE] "+String.format(message, args));
	}
	protected void ERROR(String message, Object... args ){
		if(log != null) log.error("[MOD::NODE] "+String.format(message, args));
	}
	
}

