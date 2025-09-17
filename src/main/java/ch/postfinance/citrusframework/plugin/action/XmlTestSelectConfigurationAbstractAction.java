package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.CONFIGURATION_NOT_FOUND;
import static ch.postfinance.citrusframework.plugin.UserMessages.INVALID_RUN_CONFIGURATION;
import static ch.postfinance.citrusframework.plugin.UserMessages.PROJECT_NOT_FOUND;
import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.retrieveTestFileNames;
import static ch.postfinance.citrusframework.plugin.action.RunnerArgs.D_TESTS_TO_RUN;
import static com.intellij.execution.ProgramRunnerUtil.executeConfiguration;
import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE_ARRAY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import ch.postfinance.citrusframework.plugin.dialog.RunConfigurationDialogWrapper;
import ch.postfinance.citrusframework.plugin.model.RunConfig;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract Action with the functionality for displaying a dialog to the user, so that he can select the run configuration file needed to run / debug citrus tests.
 */
public abstract class XmlTestSelectConfigurationAbstractAction
  extends XmlAbstractAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    var project = anActionEvent.getProject();
    if (isNull(project)) {
      showErrorDialog(PROJECT_NOT_FOUND);
      return;
    }

    var runManager = RunManager.getInstance(project);
    var runConfigurationsSettings = runManager.getAllSettings();
    if (runConfigurationsSettings.isEmpty()) {
      showErrorDialog(CONFIGURATION_NOT_FOUND);
      return;
    }

    VirtualFile[] virtualFiles = anActionEvent.getData(VIRTUAL_FILE_ARRAY);
    var filesName = retrieveTestFileNames(virtualFiles);

    var runConfigs = runConfigurationsSettings
      .stream()
      .map(r ->
        new RunConfig(
          r.getName(),
          r.getUniqueID().substring(0, r.getUniqueID().indexOf("."))
        )
      )
      .toList();

    var runConfigurationDialogWrapper = new RunConfigurationDialogWrapper(
      runConfigs
    );
    runConfigurationDialogWrapper.show(selectedRunConfig -> {
      Optional<
        RunnerAndConfigurationSettings
      > foundRunnerAndConfigurationSettings = getRunnerAndConfigurationSettings(
        runManager.getAllSettings(),
        selectedRunConfig.name()
      );

      foundRunnerAndConfigurationSettings.ifPresent(
        runManager::setSelectedConfiguration
      );
      var selectedConfiguration = runManager.getSelectedConfiguration();

      if (
        isNull(selectedConfiguration) ||
        !(selectedConfiguration.getConfiguration() instanceof
          JavaTestConfigurationBase javaTestConfigurationBase)
      ) {
        showErrorDialog(INVALID_RUN_CONFIGURATION);
        return;
      }

      // Returns a copy of the selected configuration.
      // This copy allows us to make changes in the configuration without modifying the original configuration.
      // So, we can add now the VM Parameters safely.
      var copyRunConfSettings = selectedConfiguration.createFactory().create();
      javaTestConfigurationBase.setName(selectedConfiguration.getName()); // using selectedConfiguration to get the name, the copy has no name per default

      var existingVMParameters = javaTestConfigurationBase.getVMParameters();
      if (nonNull(existingVMParameters)) {
        javaTestConfigurationBase.setVMParameters(
          existingVMParameters + " " + D_TESTS_TO_RUN + filesName
        );
      } else {
        javaTestConfigurationBase.setVMParameters(D_TESTS_TO_RUN + filesName);
      }

      executeConfiguration(copyRunConfSettings, getExecutor());
    });
  }

  /**
   * Get the runner and configuration settings for the selected run configuration
   *
   * @param runnerAndConfigurationSettingsList all runner and configurations settings found in the project
   * @param selectedRunConfigurationName       selected run configuration name
   * @return an Optional with the runner and configuration settings otherwise and empty Optional
   */
  private Optional<
    RunnerAndConfigurationSettings
  > getRunnerAndConfigurationSettings(
    List<RunnerAndConfigurationSettings> runnerAndConfigurationSettingsList,
    String selectedRunConfigurationName
  ) {
    return runnerAndConfigurationSettingsList
      .stream()
      .filter(s -> s.getName().equals(selectedRunConfigurationName))
      .findFirst();
  }

  public abstract Executor getExecutor();
}
