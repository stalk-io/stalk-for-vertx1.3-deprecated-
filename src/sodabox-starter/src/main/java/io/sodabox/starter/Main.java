package io.sodabox.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

public class Main extends Verticle {

	private Logger log;

	@Override
	public void start() throws Exception {

		log = container.getLogger();

		JsonObject appConfig = FileUtils.loadConfFile(container, vertx);

		Set<String> modules = appConfig.getFieldNames();

		List<Module> deployments = new ArrayList<Module>();
		for(String key : modules){
			deployments.add(new Module(appConfig.getObject(key)));
		};

		DeployModuleManager deployModule = new DeployModuleManager(container, log);

		deployModule.deployModules(deployments, new Handler<Void>() {
			public void handle(Void event) {
				log.info(" *** modules are deployed. *** ");
			}
		});
	}

}
