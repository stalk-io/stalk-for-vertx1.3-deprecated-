package io.stalk.common.api;

/**
 * 
 * 
 * @author John Kim (sodabox.io@gmail.com)
 *
 */
public interface WEB_SERVER {
	
	/* Configurations */
	String ADDRESS 			= "address";
	String HOST 			= "host";
	String PORT 			= "port";
	String GZIP_FILES		= "gzip-files";
	String WEB_ROOT			= "webRoot"; 
	String INDEX_PAGE		= "indexPage";
	String TYPE 			= "type";
			

	/* Default values */
	interface DEFAULT {
		String ADDRESS 			= "webServer";
		String HOST 			= "127.0.0.1";
		int	   PORT 			= 80;
		boolean GZIP_FILES		= false;
		String WEB_ROOT			= "webroot";
		String INDEX_PAGE		= "index.html";
		String TYPE 			= "CHAT"; 
	}
	
	/* Actions */
	interface ACTION {
		
	}
	
}