package io.sodabox.mod.subscribe;

import io.sodabox.common.api.SUB_REDIS;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

public class SubscribeModule extends BusModBase {

	private String 	address;
	private String 	host;
	private int 	port;
	private String 	channel;
	private String	replyAddress;

	private Logger log;

	public void start() {
		super.start();

		log = container.getLogger();

		address 		= getOptionalStringConfig	(SUB_REDIS.ADDRESS		, SUB_REDIS.DEFAULT.ADDRESS);
		host 			= getOptionalStringConfig	(SUB_REDIS.HOST			, SUB_REDIS.DEFAULT.HOST);
		port 			= getOptionalIntConfig		(SUB_REDIS.PORT			, SUB_REDIS.DEFAULT.PORT);
		channel 		= getMandatoryStringConfig	(SUB_REDIS.CHANNEL);
		replyAddress	= getOptionalStringConfig	(SUB_REDIS.REPLY_ADDRESS, SUB_REDIS.DEFAULT.REPLY_ADDRESS);

		// run thread!!
		new Thread(new SubscribeThread(log, eb, host, port, channel, replyAddress)).start();

		eb.registerHandler(address, new Handler<Message<JsonObject>>() {
			public void handle(Message<JsonObject> message) {

				// @ TODO 뭘 할까요?

			}
		});

	}

}