package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.retrieveTestFileNames;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract Action with the functionality for running / debugging citrus tests.
 */
public abstract class XmlTestExecuteAbstractAction extends XmlAbstractAction {

  private static final String D_TESTS_TO_RUN = "-Dtests.to.run=";
  private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found.";
  private static final String RUN_CONFIGURATION_MESSAGE =
    "Run Configuration not supported.";

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    VirtualFile[] virtualFiles = anActionEvent.getData(
      CommonDataKeys.VIRTUAL_FILE_ARRAY
    );

    //*GS2010-26866-03_DebitCards_Actions_Card_Deactivate_Test*,*GS2010-26866-04_DebitCards_Actions_Replacement_Test*
    final String filesName = retrieveTestFileNames(virtualFiles);

    Project project = anActionEvent.getProject();

    // This should actually be always false (see {@link XmlAbstractAction#update(AnActionEvent)}) is tested to be 100% sure ;-)
    if (isNull(project)) {
      showErrorDialog(PROJECT_NOT_FOUND_MESSAGE);
      return;
    }

    final RunManager runManager = RunManager.getInstance(project);
    RunnerAndConfigurationSettings selectedConfiguration =
      runManager.getSelectedConfiguration();

    // The plugin supports only run configurations that are subclasses of JavaTestConfigurationBase
    if (
      nonNull(selectedConfiguration) &&
      selectedConfiguration.getConfiguration() instanceof
      JavaTestConfigurationBase javaTestConfigurationBase
    ) {
      // Returns a copy of the selected configuration. This copy allows us to make changes in the configuration without modifying
      // the original configuration. So, we can add now the VM Parameters safety
      RunnerAndConfigurationSettings copyRunConfSettings = selectedConfiguration
        .createFactory()
        .create();
      var beforeRunTasks = selectedConfiguration
        .getConfiguration()
        .getBeforeRunTasks();
      copyRunConfSettings.getConfiguration().setBeforeRunTasks(beforeRunTasks); // set run tasks from original, clone does not include tasks
      javaTestConfigurationBase.setName(selectedConfiguration.getName()); //using selectedConfiguration to get the name, the copy has no name per default
      String existingVMParameters = javaTestConfigurationBase.getVMParameters(); //retrieve existing VM parameters
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
  }

  /**
   * Overwrite this method for setting the Executor for the test
   *
   * @return the Executor
   **/
  public abstract Executor getExecutor();
}
