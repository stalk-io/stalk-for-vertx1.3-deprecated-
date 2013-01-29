package io.sodabox.starter;

import org.vertx.java.core.json.JsonObject;

public class Module {
	private String moduleName;
	private JsonObject moduleConfig;
	private int instances = 1;

	public Module(String moduleName, JsonObject moduleConfig) {
		this.moduleName = moduleName;
		this.moduleConfig = moduleConfig;
	}

	public Module(String moduleName, JsonObject moduleConfig, Integer instances) {
		this.moduleName = moduleName;
		this.moduleConfig = moduleConfig;
		this.instances = instances.intValue();
	}
	
	public Module(JsonObject obj){
		this.moduleName = obj.getString("module");
		this.moduleConfig = obj.getObject("config");
		this.instances = obj.getInteger("instances")==null?1:obj.getInteger("instances");
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public JsonObject getModuleConfig() {
		return moduleConfig;
	}

	public void setModuleConfig(JsonObject moduleConfig) {
		this.moduleConfig = moduleConfig;
	}

	public int getInstances() {
		return instances;
	}

	public void setInstances(int instances) {
		this.instances = instances;
	}
	
	
	
}
