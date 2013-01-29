package io.sodabox.common.api;

/**
 * 
 * 
 * @author John Kim (sodabox.io@gmail.com)
 *
 */
public interface SUB_REDIS {
	
	/* Configurations */
	String ADDRESS 			= "address";
	String CHANNEL 			= "channel"; 	/** Mandatory **/
	String HOST 			= "host";
	String PORT 			= "port";
	String REPLY_ADDRESS	= "reply-address";

	/* Default values */
	interface DEFAULT {
		String ADDRESS 			= "subRedis";
		String HOST 			= "127.0.0.1";
		int	   PORT 			= 6379;
		String REPLY_ADDRESS 	= "socketServer";
	}
	
	/* Actions */
	interface ACTION {
		
	}
	
}