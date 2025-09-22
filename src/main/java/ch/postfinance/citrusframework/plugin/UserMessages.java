package ch.postfinance.citrusframework.plugin;

public final class UserMessages {

  public static final String PROJECT_NOT_FOUND = "Project not found.";
  public static final String NO_RUN_CONFIGURATION_SELECTED = """
    No existing run configuration found!
    You must create a 'run all' configuration first, which this plugin will postconfigure.
    """;
  public static final String INVALID_RUN_CONFIGURATION = """
    Unsupported type of run configuration.
    Create a 'run all' (java) run configuration first.
    """;

  public static final String CONFIGURATION_NOT_FOUND = """
    You do not have any run configurations for selection.
    Create a 'run all' (java) run configuration first.
    """;

  private UserMessages() {
    // Access to properties only
  }
}
