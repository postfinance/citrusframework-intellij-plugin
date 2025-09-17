package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.INVALID_RUN_CONFIGURATION;
import static ch.postfinance.citrusframework.plugin.UserMessages.NO_RUN_CONFIGURATION_SELECTED;
import static ch.postfinance.citrusframework.plugin.UserMessages.PROJECT_NOT_FOUND;
import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.retrieveTestFileNames;
import static ch.postfinance.citrusframework.plugin.action.RunnerArgs.D_TESTS_TO_RUN;
import static com.intellij.execution.ProgramRunnerUtil.executeConfiguration;
import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE_ARRAY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract action that provides functionality for running or debugging Citrus tests.
 * <p>
 * This action:
 * <ul>
 *   <li>Validates that a project is open</li>
 *   <li>Ensures a valid run configuration is selected</li>
 *   <li>Clones the configuration and injects test file VM parameters</li>
 *   <li>Executes the configuration with the provided {@link Executor}</li>
 * </ul>
 */
public abstract class XmlTestExecuteAbstractAction extends XmlAbstractAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    var project = event.getProject();
    if (isNull(project)) {
      showErrorDialog(PROJECT_NOT_FOUND);
      return;
    }

    var runManager = RunManager.getInstance(project);
    var selectedConfiguration = runManager.getSelectedConfiguration();

    VirtualFile[] selectedFiles = event.getData(VIRTUAL_FILE_ARRAY);
    String testFileNames = retrieveTestFileNames(selectedFiles);

    if (isNull(selectedConfiguration)) {
      showErrorDialog(NO_RUN_CONFIGURATION_SELECTED);
      return;
    }
    if (
      !(selectedConfiguration.getConfiguration() instanceof
        JavaTestConfigurationBase javaTestConfiguration)
    ) {
      showErrorDialog(INVALID_RUN_CONFIGURATION);
      return;
    }

    var clonedConfig = cloneConfiguration(
      selectedConfiguration,
      testFileNames,
      javaTestConfiguration
    );

    updateVmParameters(javaTestConfiguration, testFileNames);

    executeConfiguration(clonedConfig, getExecutor());
  }

  /**
   * Returns the executor used to run or debug the test configuration.
   *
   * @return the executor instance
   */
  public abstract Executor getExecutor();

  private RunnerAndConfigurationSettings cloneConfiguration(
    RunnerAndConfigurationSettings original,
    String testFileNames,
    JavaTestConfigurationBase javaTestConfiguration
  ) {
    RunnerAndConfigurationSettings clonedConfig = original
      .createFactory()
      .create();

    // Copy before-run tasks (clone does not include them by default)
    var beforeRunTasks = original.getConfiguration().getBeforeRunTasks();
    clonedConfig.getConfiguration().setBeforeRunTasks(beforeRunTasks);

    // Assign a descriptive name
    javaTestConfiguration.setName(original.getName() + ": " + testFileNames);

    return clonedConfig;
  }

  private void updateVmParameters(
    JavaTestConfigurationBase configuration,
    String testFileNames
  ) {
    var existingParameters = configuration.getVMParameters();
    var newParameters =
      (nonNull(existingParameters) ? existingParameters + " " : "") +
      D_TESTS_TO_RUN +
      testFileNames;
    configuration.setVMParameters(newParameters);
  }
}
