package io.stalk.mod.session;

import io.stalk.common.api.SESSION_MONITOR;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SessionMonitor extends BusModBase implements Handler<Message<JsonObject>> {

	private Logger 			log;

	private String 			address;
	private boolean 		isReady;

	private JedisPool 		redisPool;

	public void start() {

		super.start();

		log = container.getLogger();
		address = getOptionalStringConfig(SESSION_MONITOR.ADDRESS	, SESSION_MONITOR.DEFAULT.ADDRESS);

		JsonObject 	sessionConf = getOptionalObjectConfig(SESSION_MONITOR.SESSION_STORAGE, null);
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

		isReady = true;

		eb.registerHandler(address, this);
	}

	@Override
	public void stop() {
		try {
			super.stop();
			this.redisPool.destroy();
		} catch (Exception e) {
		}
	}

	@Override
	public void handle(final Message<JsonObject> message) {
		String action = message.body.getString("action");

		if(isReady){

			if(SESSION_MONITOR.ACTION.LIST.equals(action)){ 

				Jedis jedis = null;
				try{
					jedis 	= this.redisPool.getResource();

					String site = message.body.getString("site");

					Map<String, String> map = jedis.hgetAll(site);

					JsonArray pathList = new JsonArray();

					for (Map.Entry<String, String> entry : map.entrySet() )
					{
						pathList.add(entry.getKey()+","+entry.getValue());
					}

					JsonObject json = new JsonObject();
					json.putString	("site"	, site);
					json.putNumber	("count", pathList.size());
					json.putArray	("data"	, pathList);

					sendOK(message, json);

				} catch (Exception e) {
					sendError(message, e.getMessage());
					e.printStackTrace();
				} finally {
					if(jedis != null) this.redisPool.returnResource(jedis);
				}


			}else if(SESSION_MONITOR.ACTION.INFO.equals(action)){ 


				Jedis jedis = null;
				try{
					String site = message.body.getString("site");

					jedis 	= this.redisPool.getResource();

					// @ TODO have to delete !!
					/*jedis.monitor(new JedisMonitor() {
						public void onCommand(String command) {
							System.out.println(" : "+command);
						}
					});*/
					JsonObject info = new JsonObject();
					info.putString("info", jedis.info());

					sendOK(message, info);

				} catch (Exception e) {
					sendError(message, e.getMessage());
					e.printStackTrace();
				} finally {
					if(jedis != null) this.redisPool.returnResource(jedis);
				}

			}

		}else{
			sendError(message, "session storage is not existed");
		}

	}

	protected void DEBUG(String message, Object... args ){
		if(log != null) log.debug("[MOD::NODE] "+String.format(message, args));
	}
	protected void ERROR(String message, Object... args ){
		if(log != null) log.error("[MOD::NODE] "+String.format(message, args));
	}

}

