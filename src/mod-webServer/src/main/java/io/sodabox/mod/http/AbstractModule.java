package io.sodabox.mod.http;

import io.sodabox.common.api.NODE_WATCHER;
import io.sodabox.common.api.WEB_SERVER;
import io.sodabox.mod.http.oauth.SocialAuthConfig;
import io.sodabox.mod.http.oauth.SocialAuthManager;

import java.io.File;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.file.impl.PathAdjuster;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

public abstract class AbstractModule extends BusModBase implements Handler<HttpServerRequest>{

	private Logger 	log;

	private String address;
	private String 	webRoot;
	private String 	indexPage;
	private boolean gzipFiles;

	protected SocialAuthManager authManager;

	@Override
	public void start() {

		super.start();

		log = container.getLogger();

		address 		= getOptionalStringConfig	(WEB_SERVER.ADDRESS, 	WEB_SERVER.DEFAULT.ADDRESS);
		gzipFiles 		= getOptionalBooleanConfig	(WEB_SERVER.GZIP_FILES, WEB_SERVER.DEFAULT.GZIP_FILES);
		webRoot 		= getOptionalStringConfig	(WEB_SERVER.WEB_ROOT, 	WEB_SERVER.DEFAULT.WEB_ROOT);
		String index	= getOptionalStringConfig	(WEB_SERVER.INDEX_PAGE, WEB_SERVER.DEFAULT.INDEX_PAGE);
		String host		= getOptionalStringConfig	(WEB_SERVER.HOST, 		WEB_SERVER.DEFAULT.HOST);
		int	   port		= getOptionalIntConfig		(WEB_SERVER.PORT,		WEB_SERVER.DEFAULT.PORT);
		
		indexPage = webRoot + File.separator + index;

		// set socialAuthManager
		try {

			JsonObject oauthConf = getOptionalObjectConfig("oauth", null);
			SocialAuthConfig socialAuthConfig = SocialAuthConfig.getDefault();
			socialAuthConfig.load( oauthConf );

			authManager = new SocialAuthManager();
			authManager.setSocialAuthConfig(socialAuthConfig);

		} catch (Exception e) {
			e.printStackTrace();
			ERROR("init SocialManager ERROR : %s ", e.getMessage());
		}

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(this);
		server.listen(port, host);
		
		// starting watching nodes !!!!
		eb.send(NODE_WATCHER.ADDRESS, new JsonObject().putString("action", NODE_WATCHER.ACTION.START_WATCHING));

		DEBUG("Web Server is started [%s:%d]", host, port);

	}


	protected void localResponse(HttpServerRequest req) {
		// browser gzip capability check
		String acceptEncoding = req.headers().get("accept-encoding");
		boolean acceptEncodingGzip = acceptEncoding == null ? false : acceptEncoding.contains("gzip");

		String fileName = webRoot + req.path;

		if (req.path.equals("/")) {
			req.response.sendFile(indexPage);
		} else if (!req.path.contains("..")) {
			// try to send *.gz file
			/*if (gzipFiles && acceptEncodingGzip) {
				File file = new File(PathAdjuster.adjust(fileName + ".gz"));
				if (file.exists()) {
					// found file with gz extension
					req.response.putHeader("content-encoding", "gzip");
					req.response.sendFile(fileName + ".gz");
				} else {
					// not found gz file, try to send uncompressed file
					req.response.sendFile(fileName);
				}
			} else {
				// send not gzip file
				req.response.sendFile(fileName);
			}*/
			req.response.sendFile(fileName);
		} else {
			req.response.statusCode = 404;
			req.response.end();
		}

	}


	protected void DEBUG(String message, Object... args ){
		if(log != null) log.debug(String.format(message, args));
	}
	protected void ERROR(String message, Object... args ){
		if(log != null) log.debug(String.format(message, args));
	}

}
