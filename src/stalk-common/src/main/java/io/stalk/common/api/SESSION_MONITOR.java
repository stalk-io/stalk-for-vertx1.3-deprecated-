package io.stalk.common.api;

public interface SESSION_MONITOR {
	
	/* Configurations */
	String ADDRESS 			= "address";
	String SESSION_STORAGE	= "session-storage"; 	/** Mandatory **/

	/* Default values */
	interface DEFAULT {
		String ADDRESS 	= "sessionMonitor";
	}
	
	/* Actions */
	interface ACTION {
		String LIST		= "list";
		String INFO	= "info";
	}
}
