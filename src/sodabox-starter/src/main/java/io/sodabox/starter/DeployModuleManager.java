package io.sodabox.starter;

import java.util.Iterator;
import java.util.List;

import org.vertx.java.core.Handler;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Container;

public class DeployModuleManager {

	private Container container;

	private Logger log;

	public DeployModuleManager(Container container) {
		this.container = container;
	}

	public DeployModuleManager(Container container, Logger log) {
		this.container = container;
		this.log = log;
	}


	public void deployModules(final List<Module> modules, final Handler<Void> doneHandler) {

		final Iterator<Module> it = modules.iterator();

		final Handler<String> handler = new Handler<String>() {

			public void handle(String  res) {

				if(it.hasNext()) {
					Module deployment = it.next();
					log.debug("Deploying module: " + deployment.getModuleName());

					container.deployModule(
							deployment.getModuleName(), 
							deployment.getModuleConfig(), 
							deployment.getInstances(), 
							this);
				} else {

					doneHandler.handle(null);
				}

			}
		};

		handler.handle(null);
	}
}
