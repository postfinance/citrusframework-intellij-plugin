package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.NO_RUN_CONFIGURATION_SELECTED;
import static java.util.Objects.isNull;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.junit.JUnitConfiguration;

/**
 * Abstract action that provides functionality for running or debugging Citrus tests.
 * <p>
 * This action:
 * <ul>
 *   <li>Ensures a valid run configuration is selected</li>
 *   <li>Clones the configuration</li>
 * </ul>
 */
public abstract class XmlTestExecuteAbstractAction extends XmlAbstractAction {

  @Override
  @VisibleForTesting
  public RunnerAndConfigurationSettings selectRunConfiguration(
    RunManager runManager
  ) throws TestInvocationException {
    var selectedConfiguration = runManager.getSelectedConfiguration();
    if (isNull(selectedConfiguration)) {
      throw new TestInvocationException(NO_RUN_CONFIGURATION_SELECTED);
    }

    return cloneConfiguration(runManager, selectedConfiguration);
  }

  protected RunnerAndConfigurationSettings cloneConfiguration(
    RunManager runManager,
    RunnerAndConfigurationSettings selectedConfiguration
  ) {
    var clonedConfig = runManager.createConfiguration(
      PLUGIN_RUN_CONFIGURATION_NAME,
      selectedConfiguration.getFactory()
    );

    copyClassInformation(selectedConfiguration, clonedConfig);
    copyBeforeRunTasks(selectedConfiguration, clonedConfig);

    runManager.addConfiguration(clonedConfig);

    return clonedConfig;
  }

  private void copyClassInformation(
    RunnerAndConfigurationSettings selectedConfiguration,
    RunnerAndConfigurationSettings clonedConfig
  ) {
    if (
      !(selectedConfiguration.getConfiguration() instanceof
          JUnitConfiguration selectedJUnitConfiguration) ||
      !(clonedConfig.getConfiguration() instanceof
          JUnitConfiguration clonedJUnitConfiguration)
    ) return;

    clonedJUnitConfiguration.setModule(
      selectedJUnitConfiguration.getModules()[0]
    );
    clonedJUnitConfiguration.setWorkingDirectory(
      selectedJUnitConfiguration.getWorkingDirectory()
    );

    clonedJUnitConfiguration.getPersistentData().PACKAGE_NAME =
      selectedJUnitConfiguration.getPersistentData().getPackageName();
    clonedJUnitConfiguration.getPersistentData().MAIN_CLASS_NAME =
      selectedJUnitConfiguration.getPersistentData().getMainClassName();
    clonedJUnitConfiguration.getPersistentData().METHOD_NAME =
      selectedJUnitConfiguration.getPersistentData().getMethodName();
  }

  private static void copyBeforeRunTasks(
    RunnerAndConfigurationSettings source,
    RunnerAndConfigurationSettings target
  ) {
    var beforeRunTasks = source.getConfiguration().getBeforeRunTasks();
    target.getConfiguration().setBeforeRunTasks(beforeRunTasks);
  }
}
