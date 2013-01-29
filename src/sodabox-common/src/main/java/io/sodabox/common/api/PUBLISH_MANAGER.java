package io.sodabox.common.api;

public interface PUBLISH_MANAGER {
	
	/* Configurations */
	String ADDRESS 		= "address";

	/* Default values */
	interface DEFAULT {
		String ADDRESS 	= "publishManager";
	}
	
	/* Actions */
	interface ACTION {
		
		String PUB				= "pub";
		String REFRESH_NODES	= "refreshNodes";
		String DESTORY_NODES	= "destoryNodes";
		
	}
}
