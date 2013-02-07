package io.stalk.mod.mongo;

import java.net.UnknownHostException;

import io.stalk.common.api.PUBLISH_MANAGER;
import io.stalk.common.server.NodeManager;
import io.stalk.common.server.RedisNodeManager;
import io.stalk.common.server.node.RedisPoolNode;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoManager extends BusModBase implements Handler<Message<JsonObject>> {

	  private String address;
	  private String host;
	  private int port;
	  private String dbName;
	  private String username;
	  private String password;

	  private Mongo mongo;
	  private DB db;

	  public void start() {
	    super.start();

	    address = getOptionalStringConfig("address", "vertx.mongopersistor");
	    host = getOptionalStringConfig("host", "localhost");
	    port = getOptionalIntConfig("port", 27017);
	    dbName = getOptionalStringConfig("db_name", "default_db");
	    username = getOptionalStringConfig("username", null);
	    password = getOptionalStringConfig("password", null);

	    try {
	      mongo = new Mongo(host, port);
	      db = mongo.getDB(dbName);
	      if (username != null && password != null) {
	        db.authenticate(username, password.toCharArray());
	      }
	      eb.registerHandler(address, this);
	    } catch (UnknownHostException e) {
	      logger.error("Failed to connect to mongo server", e);
	    }
	  }

	  public void stop() {
	    mongo.close();
	  }

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

}
