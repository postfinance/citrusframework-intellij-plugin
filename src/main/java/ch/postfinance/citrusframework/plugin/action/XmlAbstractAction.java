package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.containsAtLeastOneTestFile;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import static java.util.Objects.nonNull;

import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class XmlAbstractAction extends AnAction {

  private static final String D_TESTS_TO_RUN = "-Dtests.to.run=";
  protected static final String ERROR = "Error";
  protected static final String INFO = "Info";

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  /**
   * Enables and sets the action visible only
   * if a project is available and the user selected a folder(s) or file(s) that is a citrus Test
   */
  @Override
  public void update(@NotNull AnActionEvent anActionEvent) {
    VirtualFile[] virtualFiles = anActionEvent.getData(
      CommonDataKeys.VIRTUAL_FILE_ARRAY
    );
    anActionEvent
      .getPresentation()
      .setEnabledAndVisible(
        nonNull(anActionEvent.getProject()) &&
          nonNull(virtualFiles) &&
          containsAtLeastOneTestFile(virtualFiles)
      );
  }

  protected void executeWithTestFiles(
    RunnerAndConfigurationSettings selectedConfiguration,
    String testFileNames,
    Executor executor
  ) {
    if (
      !(selectedConfiguration.getConfiguration() instanceof
          JavaTestConfigurationBase originalConfig)
    ) {
      String configType = selectedConfiguration
        .getConfiguration()
        .getType()
        .getDisplayName();
      showErrorDialog(
        "The selected run configuration '" +
          selectedConfiguration.getName() +
          "' is of type '" +
          configType +
          "', which is not supported. " +
          "Please select a JUnit or TestNG run configuration."
      );
      return;
    }

    String existingVMParameters = originalConfig.getVMParameters();

    RunnerAndConfigurationSettings copyRunConfSettings = selectedConfiguration
      .createFactory()
      .create();
    JavaTestConfigurationBase copyConfig =
      (JavaTestConfigurationBase) copyRunConfSettings.getConfiguration();

    copyConfig.setBeforeRunTasks(originalConfig.getBeforeRunTasks());
    copyConfig.setName(selectedConfiguration.getName());
    if (nonNull(existingVMParameters)) {
      copyConfig.setVMParameters(
        existingVMParameters + " " + D_TESTS_TO_RUN + testFileNames
      );
    } else {
      copyConfig.setVMParameters(D_TESTS_TO_RUN + testFileNames);
    }

    ProgramRunnerUtil.executeConfiguration(copyRunConfSettings, executor);
  }

  public void showErrorDialog(String errorMessage) {
    showMessageDialog(errorMessage, ERROR, Messages.getErrorIcon());
  }

  public void showInfoDialog(String infoMessage) {
    showMessageDialog(infoMessage, INFO, Messages.getInformationIcon());
  }
}
