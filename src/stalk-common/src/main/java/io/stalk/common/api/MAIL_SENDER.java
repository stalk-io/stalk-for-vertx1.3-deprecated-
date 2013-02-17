package io.stalk.common.api;

/**
 * 
 * 
 * @author John Kim (sodabox.io@gmail.com)
 *
 */
public interface MAIL_SENDER {
	
	/* Default values */
	interface DEFAULT {
		String ADDRESS 			= "mailSender";
	}
	
	/* Actions */
	interface ACTION {
		
		/**
		 * create server node to Zookeeper directory.
		 * input : channel, data
		 */
		String CREATE_NODE		= "createNode";
		
		String START_WATCHING	= "startWatching";

		String DEL_NODE		= "delNode"; 

		String INFO_CHANNEL		= "infoChannel"; 
		
	}
	
}