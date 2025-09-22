package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.CONFIGURATION_NOT_FOUND;
import static ch.postfinance.citrusframework.plugin.UserMessages.NO_RUN_CONFIGURATION_SELECTED;
import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;

import ch.postfinance.citrusframework.plugin.dialog.RunConfigurationDialogWrapper;
import ch.postfinance.citrusframework.plugin.model.RunConfig;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Abstract Action with the functionality for displaying a dialog to the user, so that it can select the run configuration file needed to run / debug citrus tests.
 */
public abstract class XmlTestSelectConfigurationAbstractAction
  extends XmlTestExecuteAbstractAction {

  @Override
  @VisibleForTesting
  public RunnerAndConfigurationSettings selectRunConfiguration(
    RunManager runManager
  ) throws TestInvocationException {
    var runConfigurationsSettings = runManager.getAllSettings();
    if (runConfigurationsSettings.isEmpty()) {
      throw new TestInvocationException(CONFIGURATION_NOT_FOUND);
    }

    var runConfigs = runConfigurationsSettings
      .stream()
      .map(r ->
        new RunConfig(
          r.getName(),
          r.getUniqueID().substring(0, r.getUniqueID().indexOf("."))
        )
      )
      .toList();

    var runnerAndConfigurationSettings = new ArrayBlockingQueue<
      RunnerAndConfigurationSettings
    >(1);

    openDialogAndWaitForRunnerAndConfigurationSettingsSelection(
      runManager,
      runConfigs,
      runnerAndConfigurationSettings
    );

    return runnerAndConfigurationSettings.poll();
  }

  private void openDialogAndWaitForRunnerAndConfigurationSettingsSelection(
    RunManager runManager,
    List<RunConfig> runConfigs,
    ArrayBlockingQueue<
      RunnerAndConfigurationSettings
    > runnerAndConfigurationSettings
  ) {
    var runConfigurationDialogWrapper = new RunConfigurationDialogWrapper(
      runConfigs
    );
    runConfigurationDialogWrapper.show(selectedRunConfig -> {
      Optional<
        RunnerAndConfigurationSettings
      > foundRunnerAndConfigurationSettings =
        getSelectedRunnerAndConfigurationSettings(
          runManager.getAllSettings(),
          selectedRunConfig.name()
        );

      foundRunnerAndConfigurationSettings.ifPresent(
        runManager::setSelectedConfiguration
      );
      var selectedConfiguration = runManager.getSelectedConfiguration();

      if (isNull(selectedConfiguration)) {
        throw new TestInvocationException(NO_RUN_CONFIGURATION_SELECTED);
      }

      if (
        !selectedConfiguration.getName().equals(PLUGIN_RUN_CONFIGURATION_NAME)
      ) {
        selectedConfiguration = cloneConfiguration(
          runManager,
          selectedConfiguration
        );
      }

      try {
        runnerAndConfigurationSettings.put(selectedConfiguration);
      } catch (InterruptedException e) {
        currentThread().interrupt();
        throw new IllegalStateException(
          "Unreachable state: Internal settings queue can never be full!"
        );
      }
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
  > getSelectedRunnerAndConfigurationSettings(
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
