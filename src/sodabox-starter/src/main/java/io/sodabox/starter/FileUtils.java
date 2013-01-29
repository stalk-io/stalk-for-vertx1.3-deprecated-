package io.sodabox.starter;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Container;

public class FileUtils {
	
	public static String DEFAULT_CONF_FILE = "conf/defaultConf.json";

	public static String loadStaticData(Vertx vertx, String filename) throws Exception {
		if (filename == null || filename.isEmpty()) {
			return null;
		}

		Buffer buffer = null;
		String data = null;

		buffer = vertx.fileSystem().readFileSync(filename);
		data = buffer.getString(0, buffer.length());

		return data;
	}
	
	public static JsonObject loadConfFile(Vertx vertx, String filename) throws Exception {
		String confStr = loadStaticData(vertx, filename);
		return new JsonObject(confStr);
	}
	
	public static JsonObject loadConfFile(Vertx vertx) throws Exception {
		return loadConfFile(vertx, DEFAULT_CONF_FILE);
	}
	
	public static JsonObject loadConfFile(Container container, Vertx vertx) throws Exception{
		JsonObject appConfig = container.getConfig();
		if(appConfig.size() == 0) appConfig = loadConfFile(vertx);
		return appConfig;
	}
}
