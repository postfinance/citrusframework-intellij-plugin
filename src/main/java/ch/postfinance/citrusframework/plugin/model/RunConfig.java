package ch.postfinance.citrusframework.plugin.model;

public class RunConfig {

  private final String name;
  private final String type;

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
