package io.sodabox.common.api;

/**
 * 
 * 
 * @author John Kim (sodabox.io@gmail.com)
 *
 */
public interface SOCKET_SERVER {
	
	/* Configurations */
	String ADDRESS 			= "address";
	String CHANNEL 			= "channel"; 	/** Mandatory **/
	String HOST 			= "host";		/** Mandatory **/
	String PORT 			= "port";		/** Mandatory **/

	/* Default values */
	interface DEFAULT {
		String ADDRESS 			= "socketServer";
		String HOST 			= "0.0.0.0";
		int	   PORT 			= 80;
	}
	
	/* Actions */
	interface ACTION {
		
		String MESSAGE		= "message";
		String SUBSCRIBE	= "subscribe";
		String UNSUBSCRIBE	= "unsubscribe";
		
	}
	
}