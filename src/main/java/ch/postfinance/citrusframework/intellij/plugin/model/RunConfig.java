package ch.postfinance.citrusframework.intellij.plugin.model;

public class RunConfig {

	private String name;
	private String type;

	public RunConfig(String name, String type) {
		this.name = name;
		this.type = type;

	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
}
