package io.sodabox.common.api;

/**
 * 
 * 
 * @author John Kim (sodabox.io@gmail.com)
 *
 */
public interface NODE_WATCHER {
	
	/* Configurations */
	String ADDRESS 			= "address";
	String ZOOKEEPER_SERVERS= "zookeeper-servers"; 	/** Mandatory **/
	String TIMEOUT 			= "timeout";	
	String ROOT_PATH 		= "rootPath";

	/* Default values */
	interface DEFAULT {
		String ADDRESS 			= "nodeWatcher";
		int	   TIMEOUT 			= 1000;
		String ROOT_PATH 		= "/SODABOX/node";
	}
	
	/* Actions */
	interface ACTION {
		
		/**
		 * create server node to Zookeeper directory.
		 * input : channel, data
		 */
		String CREATE_NODE		= "createNode";
		
		String START_WATCHING	= "startWatching";
		
	}
	
}