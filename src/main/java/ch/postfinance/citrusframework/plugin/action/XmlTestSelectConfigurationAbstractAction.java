package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.retrieveTestFileNames;
import static java.util.Objects.isNull;

import ch.postfinance.citrusframework.plugin.dialog.RunConfigurationDialogWrapper;
import ch.postfinance.citrusframework.plugin.model.RunConfig;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract Action with the functionality for displaying a dialog to the user, so that
 * he can select the run configuration file needed to run / debug citrus tests.
 */
public abstract class XmlTestSelectConfigurationAbstractAction
  extends XmlAbstractAction
{

  private static final String PROJECT_NOT_FOUND = "Project not found.";
  private static final String CONFIGURATION_NOT_FOUND_MESSAGE =
    "No run configurations available. Please create a run configuration first.";

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    Project project = anActionEvent.getProject();
    if (isNull(project)) {
      showErrorDialog(PROJECT_NOT_FOUND);
      return;
    }

    RunManager runManager = RunManager.getInstance(project);
    List<RunnerAndConfigurationSettings> runConfigurationsSettings =
      runManager.getAllSettings();

    if (runConfigurationsSettings.isEmpty()) {
      showInfoDialog(CONFIGURATION_NOT_FOUND_MESSAGE);
      return;
    }

    VirtualFile[] virtualFiles = anActionEvent.getData(
      CommonDataKeys.VIRTUAL_FILE_ARRAY
    );
    String testFileNames = retrieveTestFileNames(virtualFiles);

    List<RunConfig> runConfigs = runConfigurationsSettings
      .stream()
      .map(r -> new RunConfig(r.getName(), extractConfigType(r.getUniqueID())))
      .toList();

    RunConfigurationDialogWrapper runConfigurationDialogWrapper =
      new RunConfigurationDialogWrapper(runConfigs);
    runConfigurationDialogWrapper.show(selectedRunConfig -> {
      Optional<RunnerAndConfigurationSettings> foundSettings =
        findConfigurationByName(
          runManager.getAllSettings(),
          selectedRunConfig.getName()
        );

      foundSettings.ifPresent(runManager::setSelectedConfiguration);
      RunnerAndConfigurationSettings selectedConfiguration =
        runManager.getSelectedConfiguration();

      if (isNull(selectedConfiguration)) {
        showErrorDialog("No run configuration is currently selected.");
        return;
      }

      executeWithTestFiles(selectedConfiguration, testFileNames, getExecutor());
    });
  }

  static String extractConfigType(String uniqueId) {
    int dotIndex = uniqueId.indexOf(".");
    return dotIndex >= 0 ? uniqueId.substring(0, dotIndex) : uniqueId;
  }

  private Optional<RunnerAndConfigurationSettings> findConfigurationByName(
    List<RunnerAndConfigurationSettings> settings,
    String name
  ) {
    return settings
      .stream()
      .filter(s -> s.getName().equals(name))
      .findFirst();
  }

  public abstract Executor getExecutor();
}
