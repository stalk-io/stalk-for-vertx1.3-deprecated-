package io.stalk.common.api;

/**
 * 
 * 
 * @author John Kim (sodabox.io@gmail.com)
 *
 */
public interface MONGO_MANAGER {
	
	interface DEFAULT {
		String ADDRESS 		= "mongoManager";
	}
	
	/* Actions */
	interface ACTION {

		String NEW			= "new";
		String UPDATE		= "update";
		String REMOVE		= "remove";
		String AUTH			= "auth";
		String GET			= "get";
		
	}
	
	interface ERR {

		String NOT_EXISTED 		= "notExisted";
		String NOT_VALID_CODE 	= "notValidCode";
		String INTERNAL 		= "internalException";
	}
	
}