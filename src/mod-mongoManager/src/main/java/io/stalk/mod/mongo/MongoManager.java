package io.stalk.mod.mongo;

import io.stalk.common.utils.BijectiveUtils;

import java.net.UnknownHostException;
import java.util.UUID;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class MongoManager extends BusModBase implements Handler<Message<JsonObject>> {

	private String address;
	private String host;
	private int port;
	private String dbName;
	private String username;
	private String password;
	private String collection;

	private Mongo mongo;
	private DB db;
	private DBCollection dbCollection;

	@Override
	public void start() {
		super.start();

		address = getOptionalStringConfig("address", "vertx.mongopersistor");
		host = getOptionalStringConfig("host", "localhost");
		port = getOptionalIntConfig("port", 27017);
		dbName = getOptionalStringConfig("db_name", "default_db");
		username = getOptionalStringConfig("username", null);
		password = getOptionalStringConfig("password", null);
		collection = getOptionalStringConfig("collection", "users");

		try {
			mongo = new Mongo(host, port);
			db = mongo.getDB(dbName);
			if (username != null && password != null) {
				db.authenticate(username, password.toCharArray());
			}
			dbCollection = db.getCollection(collection);

			eb.registerHandler(address, this);
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
		case "save":
			doSave(message);
			break;
		case "update":
			doUpdate(message);
			break;    
		case "find":
			doFind(message);
			break;
		case "findone":
			doFindOne(message);
			break;
		case "delete":
			doDelete(message);
			break;
		case "count":
			doCount(message);
			break;
		case "getCollections":
			getCollections(message);
			break;
		case "collectionStats":
			getCollectionStats(message);
			break;
		case "command":
			runCommand(message);
		default:
			sendError(message, "Invalid action: " + action);
			return;
		}
	}

	private void createUser(Message<JsonObject> message) {

		int seq = getSeq("user_email");

		String key = BijectiveUtils.encode(seq);

		WriteConcern writeConcern = WriteConcern.valueOf(getOptionalStringConfig("writeConcern",""));
		if (writeConcern == null) writeConcern = db.getWriteConcern();
		
		JsonObject doc = getMandatoryObject("document", message);
		
		DBObject obj = jsonToDBObject(doc);
		WriteResult res = dbCollection.save(obj, writeConcern);
		if (res.getError() == null) {
			JsonObject reply = new JsonObject();
			reply.putString("key", key);
			sendOK(message, reply);
		} else {
			sendError(message, res.getError());
		}

	}

	private void updateUser(Message<JsonObject> message) {
		
		
		
	}

	private void doSave(Message<JsonObject> message) {
		String collection = getMandatoryString("collection", message);
		if (collection == null) {
			return;
		}
		JsonObject doc = getMandatoryObject("document", message);
		if (doc == null) {
			return;
		}
		String genID;
		if (doc.getField("_id") == null) {
			genID = UUID.randomUUID().toString();
			doc.putString("_id", genID);
		} else {
			genID = null;
		}
		DBCollection coll = db.getCollection(collection);
		DBObject obj = jsonToDBObject(doc);
		WriteConcern writeConcern = WriteConcern.valueOf(getOptionalStringConfig("writeConcern",""));
		if (writeConcern == null) {
			writeConcern = db.getWriteConcern();
		}
		WriteResult res = coll.save(obj, writeConcern);
		if (res.getError() == null) {
			if (genID != null) {
				JsonObject reply = new JsonObject();
				reply.putString("_id", genID);
				sendOK(message, reply);
			} else {
				sendOK(message);
			}
		} else {
			sendError(message, res.getError());
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
	
	private DBObject jsonToDBObject(JsonObject object) {
		String str = object.encode();
		return (DBObject)JSON.parse(str);
	}
	
}
