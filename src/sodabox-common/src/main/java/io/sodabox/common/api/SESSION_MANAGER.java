package io.sodabox.common.api;

public interface SESSION_MANAGER {
	
	/* Configurations */
	String ADDRESS 			= "address";
	String SESSION_STORAGE	= "session-storage"; 	/** Mandatory **/

	/* Default values */
	interface DEFAULT {
		String ADDRESS 	= "sessionManager";
	}
	
	/* Actions */
	interface ACTION {
		
		/**
		 * create session info in session redis and get dedicated server data
		 * input : action, refer
		 */
		String IN		= "in";
		
		/**
		 * where users login-outed, apply user count to session redis.
		 * input : action, refer, count
		 */
		String UPDATE		= "update";
		
		/**
		 * get access lists
		 * input : action, domain
		 */
		String LIST		= "list";
		

		String REFRESH_NODES	= "refreshNodes";
		String DESTORY_NODES	= "destoryNodes";
		
	}
}
