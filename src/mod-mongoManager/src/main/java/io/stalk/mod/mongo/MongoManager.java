package io.stalk.mod.mongo;

import io.stalk.common.api.MAIL_SENDER;
import io.stalk.common.api.MONGO_MANAGER;
import io.stalk.common.api.MongoManagerConfig;
import io.stalk.common.utils.BijectiveUtils;
import io.stalk.common.utils.PasswordUtils;

import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class MongoManager extends BusModBase implements Handler<Message<JsonObject>> {

	private MongoManagerConfig moduleConfig;

	private Mongo mongo;
	private DB db;
	private DBCollection dbCollection;

	@Override
	public void start() {
		super.start();

		moduleConfig = new MongoManagerConfig(config);

		try {
			mongo = new Mongo(
					moduleConfig.getServer().getHost(), 
					moduleConfig.getServer().getPort()
					);

			db = mongo.getDB(moduleConfig.getDbName());

			if (
					moduleConfig.getServer().getUsername() != null && 
					moduleConfig.getServer().getPassword() != null) {

				db.authenticate(
						moduleConfig.getServer().getUsername(), 
						moduleConfig.getServer().getPassword().toCharArray());

			}

			dbCollection = db.getCollection(moduleConfig.getCollection());

			eb.registerHandler(moduleConfig.getAddress(), this);

		} catch (UnknownHostException e) {
			logger.error("Failed to connect to mongo server", e);
		}
	}

	@Override
	public void stop() {
		mongo.close();
	}

	@Override
	public void handle(Message<JsonObject> message) {

		String action = message.body.getString("action");

		if (action == null) {
			sendError(message, "action must be specified");
			return;
		}

		switch (action) {
		case MONGO_MANAGER.ACTION.NEW:
			doNew(message);
			break;
		case MONGO_MANAGER.ACTION.AUTH:
			doAuth(message);
			break;    
		default:
			sendError(message, "Invalid action: " + action);
			return;
		}

	}

	private void doNew(Message<JsonObject> message) {

		String email 	= message.body.getString("email");
		String name 	= message.body.getString("name");
		String password = message.body.getString("password");

		int seq = getSeq("user");
		String key = BijectiveUtils.encode(seq);

		// insert !!
		JsonObject user = new JsonObject();
		user.putString("_id"		, key);
		user.putString("email"		, email);
		user.putString("name"		, name);
		user.putString("password"	, PasswordUtils.encrypt(password));

		DBObject obj = (DBObject)JSON.parse(user.encode());
		WriteResult res = dbCollection.save(obj);

		if (res.getError() == null) {

			// send mail !!
			JsonObject mailSender = new JsonObject();
			mailSender.putString("email", email);
			mailSender.putString("name"	, name);
			mailSender.putString("id"	, key);
			mailSender.putString("auth"	, RandomStringUtils.randomAlphabetic(10));

			eb.send(MAIL_SENDER.DEFAULT.ADDRESS, mailSender, new Handler<Message<JsonObject>>() {
				public void handle(Message<JsonObject> msg) {
					System.out.println(msg.body.encode());
				}
			});

			JsonObject reply = new JsonObject();
			reply.putString("key", key);
			sendOK(message, reply);
		} else {
			sendError(message, res.getError());
		}


	}

	private void doAuth(Message<JsonObject> message) {

		String id 	= message.body.getString("id");
		String auth = message.body.getString("auth");

		JsonObject user = findOne("_id", id);
		
		if(user != null){
			String a = user.getString("auth");
			if(!StringUtils.isEmpty(a) && a.equals(auth)){

				DBObject query = new BasicDBObject();
				query.put("_id", id);

				DBObject change = new BasicDBObject("use", true);
				DBObject update = new BasicDBObject("$set", change);

				WriteResult res = dbCollection.update(query, update);
				
				if (res.getError() == null) {
					sendOK(message);
				} else {
					// res.getError()
					sendError(message, MONGO_MANAGER.ERR.INTERNAL);
				}
				
			}else{
				sendError(message, MONGO_MANAGER.ERR.NOT_VALID_CODE);
			}
		}else{
			sendError(message, MONGO_MANAGER.ERR.NOT_EXISTED);
		}

	}


	private void getCollectionStats(Message<JsonObject> message) {
		String collection = getMandatoryString("collection", message);

		if (collection == null) {
			return;
		}

		DBCollection coll = db.getCollection(collection);
		CommandResult stats = coll.getStats();

		JsonObject reply = new JsonObject();
		reply.putObject("stats", new JsonObject(stats.toString()));
		sendOK(message, reply);

	}


	private JsonObject findOne(String col, String value) {

		DBObject query = new BasicDBObject(col, value);
		DBObject res = dbCollection.findOne(null, query);

		if (res != null) {
			String s = res.toString();
			JsonObject m = new JsonObject(s);
			return m;
		}else{
			return null;
		}
	}


	private int getSeq(String seqName) {
		String sequence_collection = "seqs"; 
		String sequence_field = "seq";

		DBCollection seq = db.getCollection(sequence_collection);

		DBObject query = new BasicDBObject();
		query.put("_id", seqName);

		DBObject change = new BasicDBObject(sequence_field, 1);
		DBObject update = new BasicDBObject("$inc", change);

		DBObject res = seq.findAndModify(query, new BasicDBObject(), new BasicDBObject(), false, update, true, true);
		return Integer.parseInt(res.get(sequence_field).toString());
	}

}
