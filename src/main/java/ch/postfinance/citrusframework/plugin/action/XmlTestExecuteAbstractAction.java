package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.INVALID_RUN_CONFIGURATION;
import static ch.postfinance.citrusframework.plugin.UserMessages.PROJECT_NOT_FOUND;
import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.retrieveTestFileNames;
import static ch.postfinance.citrusframework.plugin.action.RunnerArgs.D_TESTS_TO_RUN;
import static com.intellij.execution.ProgramRunnerUtil.executeConfiguration;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract Action with the functionality for running / debugging citrus tests.
 */
public abstract class XmlTestExecuteAbstractAction extends XmlAbstractAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    var project = anActionEvent.getProject();
    if (isNull(project)) {
      showErrorDialog(PROJECT_NOT_FOUND);
      return;
    }

    var runManager = RunManager.getInstance(project);
    var selectedConfiguration = runManager.getSelectedConfiguration();

    VirtualFile[] virtualFiles = anActionEvent.getData(
      CommonDataKeys.VIRTUAL_FILE_ARRAY
    );

    var filesName = retrieveTestFileNames(virtualFiles);

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
    RunnerAndConfigurationSettings copyRunConfSettings = selectedConfiguration
      .createFactory()
      .create();
    var beforeRunTasks = selectedConfiguration
      .getConfiguration()
      .getBeforeRunTasks();
    copyRunConfSettings.getConfiguration().setBeforeRunTasks(beforeRunTasks); // set run tasks from original, clone does not include tasks
    javaTestConfigurationBase.setName(selectedConfiguration.getName()); //using selectedConfiguration to get the name, the copy has no name per default

    var existingVMParameters = javaTestConfigurationBase.getVMParameters(); //retrieve existing VM parameters
    if (nonNull(existingVMParameters)) {
      javaTestConfigurationBase.setVMParameters(
        existingVMParameters + " " + D_TESTS_TO_RUN + filesName
      );
    } else {
      javaTestConfigurationBase.setVMParameters(D_TESTS_TO_RUN + filesName);
    }

    executeConfiguration(copyRunConfSettings, getExecutor());
  }

  public abstract Executor getExecutor();
}
