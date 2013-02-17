package io.stalk.common.api;

import org.vertx.java.core.json.JsonObject;

public class MongoManagerConfig {

	private String address;
	private String dbName;
	private String collection;
	private Server server;

	public MongoManagerConfig(JsonObject json) {
		this.address 	= json.getString("address"		, "mongoManager");
		this.dbName 	= json.getString("db_name"		, "stalk");
		this.collection = json.getString("collection"	, "users");

		JsonObject serverConf = json.getObject("server");
		this.server = new Server(serverConf);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}



	public class Server {

		private String 	host = "localhost";
		private int 	port = 27017;
		private String 	username;
		private String 	password;

		public Server(JsonObject json) {

			if(json != null){
				this.host 		= json.getString("host", "localhost");
				this.port 		= json.getNumber("port", 27017).intValue();
				this.username	= json.getString("username", null);
				this.password	= json.getString("password", null);
			}
		}

		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}


	}
}
