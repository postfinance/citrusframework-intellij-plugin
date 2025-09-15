package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.retrieveTestFileNames;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import ch.postfinance.citrusframework.plugin.dialog.RunConfigurationDialogWrapper;
import ch.postfinance.citrusframework.plugin.model.RunConfig;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract Action with the functionality for displaying a dialog to the user, so that
 * he can select the run configuration file needed to run / debug citrus tests.
 */
public abstract class XmlTestSelectConfigurationAbstractAction
  extends XmlAbstractAction {

  private static final String D_TESTS_TO_RUN = "-Dtests.to.run=";
  private static final String PROJECT_NOT_FOUND = "Project not found.";
  private static final String RUN_CONFIGURATION_MESSAGE =
    "Run Configuration not supported.";
  private static final String CONFIGURATION_NOT_FOUND_MESSAGE =
    "Run Configuration not found.";

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    Project project = anActionEvent.getProject();
    // This should actually be always false (see {@link XmlAbstractAction#update(AnActionEvent)}) is tested to be 100% sure ;-)
    if (isNull(project)) {
      showErrorDialog(PROJECT_NOT_FOUND);
      return;
    }

    RunManager runManager = RunManager.getInstance(project);
    List<RunnerAndConfigurationSettings> runConfigurationsSettings =
      runManager.getAllSettings();
    //No run configuration found
    if (runConfigurationsSettings.isEmpty()) {
      showErrorDialog(CONFIGURATION_NOT_FOUND_MESSAGE);
      return;
    }

    //Get the selected test files by the user
    VirtualFile[] virtualFiles = anActionEvent.getData(
      CommonDataKeys.VIRTUAL_FILE_ARRAY
    );
    //*GS2010-26866-03_DebitCards_Actions_Card_Deactivate_Test*,*GS2010-26866-04_DebitCards_Actions_Replacement_Test*
    String filesName = retrieveTestFileNames(virtualFiles);

    List<RunConfig> runConfigs = runConfigurationsSettings
      .stream()
      //JUnit.someName or Maven.someName
      .map(r ->
        new RunConfig(
          r.getName(),
          r.getUniqueID().substring(0, r.getUniqueID().indexOf("."))
        )
      )
      .collect(Collectors.toList());

    RunConfigurationDialogWrapper runConfigurationDialogWrapper =
      new RunConfigurationDialogWrapper(runConfigs);
    runConfigurationDialogWrapper.show(selectedRunConfig -> {
      Optional<
        RunnerAndConfigurationSettings
      > foundRunnerAndConfigurationSettings = getRunnerAndConfigurationSettings(
        runManager.getAllSettings(),
        selectedRunConfig.name()
      );

      //Set the new selected configuration settings to the RunManager
      foundRunnerAndConfigurationSettings.ifPresent(
        runManager::setSelectedConfiguration
      );
      RunnerAndConfigurationSettings selectedConfiguration =
        runManager.getSelectedConfiguration();
      //The plugin supports only run configurations that are subclasses of JavaTestConfigurationBase
      if (
        nonNull(selectedConfiguration) &&
        selectedConfiguration.getConfiguration() instanceof
        JavaTestConfigurationBase javaTestConfigurationBase
      ) {
        // Returns a copy of the selected configuration. This copy allows us to make changes in the configuration without modifying
        // the original configuration. So, we can add now the VM Parameters safety
        RunnerAndConfigurationSettings copyRunConfSettings =
          selectedConfiguration.createFactory().create();
        javaTestConfigurationBase.setName(selectedConfiguration.getName()); //using selectedConfiguration to get the name, the copy has no name per default
        String existingVMParameters =
          javaTestConfigurationBase.getVMParameters(); //retrieve existing VM parameters
        if (nonNull(existingVMParameters)) {
          javaTestConfigurationBase.setVMParameters(
            existingVMParameters + " " + D_TESTS_TO_RUN + filesName
          );
        } else {
          javaTestConfigurationBase.setVMParameters(D_TESTS_TO_RUN + filesName);
        }
        ProgramRunnerUtil.executeConfiguration(
          copyRunConfSettings,
          getExecutor()
        );
      } else {
        showErrorDialog(RUN_CONFIGURATION_MESSAGE);
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
  > getRunnerAndConfigurationSettings(
    List<RunnerAndConfigurationSettings> runnerAndConfigurationSettingsList,
    String selectedRunConfigurationName
  ) {
    return runnerAndConfigurationSettingsList
      .stream()
      .filter(s -> s.getName().equals(selectedRunConfigurationName))
      .findFirst();
  }

  /**
   * Overwrite this method for setting the Executor for the test
   *
   * @return the Executor
   */
  public abstract Executor getExecutor();
}
