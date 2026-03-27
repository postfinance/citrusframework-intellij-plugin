package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.retrieveTestFileNames;
import static java.util.Objects.isNull;

import com.intellij.execution.Executor;
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

  private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found.";

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    VirtualFile[] virtualFiles = anActionEvent.getData(
      CommonDataKeys.VIRTUAL_FILE_ARRAY
    );
    String testFileNames = retrieveTestFileNames(virtualFiles);

    Project project = anActionEvent.getProject();
    if (isNull(project)) {
      showErrorDialog(PROJECT_NOT_FOUND_MESSAGE);
      return;
    }

    RunnerAndConfigurationSettings selectedConfiguration =
      RunManager.getInstance(project).getSelectedConfiguration();

    if (isNull(selectedConfiguration)) {
      showInfoDialog(
        "No run configuration is currently selected. Please select a run configuration first."
      );
      return;
    }

    executeWithTestFiles(selectedConfiguration, testFileNames, getExecutor());
  }

  public abstract Executor getExecutor();
}
