package io.stalk.common.api;

public interface MONITOR_SERVER {

	/* Configurations */
	String ADDRESS 			= "address";
	String HOST 			= "host";
	String PORT 			= "port";

	/* Default values */
	interface DEFAULT {
		String ADDRESS 			= "webServer";
		String HOST 			= "127.0.0.1";
		int	   PORT 			= 80;
	}

}