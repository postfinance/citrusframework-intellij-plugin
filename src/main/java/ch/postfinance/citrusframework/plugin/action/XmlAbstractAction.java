package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.INVALID_RUN_CONFIGURATION;
import static ch.postfinance.citrusframework.plugin.UserMessages.PROJECT_NOT_FOUND;
import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.containsAtLeastOneTestFile;
import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.retrieveTestFileNames;
import static ch.postfinance.citrusframework.plugin.action.RunnerArgs.D_TESTS_TO_RUN;
import static com.intellij.execution.ProgramRunnerUtil.executeConfiguration;
import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE_ARRAY;
import static com.intellij.openapi.actionSystem.PlatformCoreDataKeys.SELECTED_ITEMS;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base action for operations on Citrus XML test files.
 * <p>
 * Provides:
 * <ul>
 *   <li>Automatic enable/disable visibility depending on project context and selected files</li>
 *   <li>Project validation and default configuration selection, if present</li>
 *   <li>Utility method for showing error dialogs</li>
 * </ul>
 */
public abstract class XmlAbstractAction extends AnAction {

  protected static final String PLUGIN_RUN_CONFIGURATION_NAME =
    "Citrus XML Test Runner";
  protected static final String ERROR = "Error";

  /**
   * Updates the availability of this action based on the current selection.
   * <p>
   * The action is only enabled and visible if:
   * <ul>
   *   <li>A project is open</li>
   *   <li>The user has selected at least one file/folder that is recognized as a Citrus test</li>
   * </ul>
   *
   * @param event the action event carrying context information
   */
  @Override
  public void update(@NotNull AnActionEvent event) {
    var dataItems = event.getData(SELECTED_ITEMS);
    if (isNull(dataItems)) {
      return;
    }

    var virtualFiles = Arrays.stream(dataItems)
      .map(selectedItem ->
        switch (selectedItem) {
          case BasePsiNode basePsiNode -> basePsiNode.getVirtualFile();
          case VirtualFile virtualFile -> virtualFile;
          default -> null;
        }
      )
      .filter(Objects::nonNull)
      .toArray(VirtualFile[]::new);

    event
      .getPresentation()
      .setEnabledAndVisible(
        nonNull(event.getProject()) && containsAtLeastOneTestFile(virtualFiles)
      );
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    var project = event.getProject();
    if (isNull(project)) {
      showErrorDialog(PROJECT_NOT_FOUND);
      return;
    }

    var runManager = RunManager.getInstance(project);

    try {
      executeSelectedRunConfiguration(event, runManager);
    } catch (TestInvocationException e) {
      showErrorDialog(e.getDialogMessage());
    }
  }

  private void executeSelectedRunConfiguration(
    AnActionEvent event,
    RunManager runManager
  ) {
    var runConfiguration = runManager
      .getAllSettings()
      .stream()
      .filter(settings ->
        settings.getName().equals(PLUGIN_RUN_CONFIGURATION_NAME)
      )
      .findFirst()
      .orElseGet(() -> selectRunConfiguration(runManager));

    if (
      !(runConfiguration.getConfiguration() instanceof
          JavaTestConfigurationBase javaTestConfigurationBase)
    ) {
      throw new TestInvocationException(INVALID_RUN_CONFIGURATION);
    }

    VirtualFile[] selectedFiles = event.getData(VIRTUAL_FILE_ARRAY);
    String testFileNames = retrieveTestFileNames(selectedFiles);

    updateVmParameters(javaTestConfigurationBase, testFileNames);

    runManager.setSelectedConfiguration(runConfiguration);

    executeConfiguration(runConfiguration, getExecutor());
  }

  protected abstract @Nonnull RunnerAndConfigurationSettings selectRunConfiguration(
    RunManager runManager
  ) throws TestInvocationException;

  private void updateVmParameters(
    JavaTestConfigurationBase javaTestConfigurationBase,
    String testFileNames
  ) {
    var existingParameters = javaTestConfigurationBase.getVMParameters();

    String newParameters;
    if (isBlank(existingParameters)) {
      newParameters = D_TESTS_TO_RUN + testFileNames;
    } else {
      newParameters = constructNewVmParameters(
        testFileNames,
        existingParameters
      );
    }

    javaTestConfigurationBase.setVMParameters(newParameters.trim());
  }

  private static @NotNull String constructNewVmParameters(
    String testFileNames,
    String existingParameters
  ) {
    var newParameters = isNotBlank(existingParameters)
      ? existingParameters
      : "";
    // Remove any existing -Dtests.to.run= parameter and its value
    if (existingParameters.contains(D_TESTS_TO_RUN)) {
      String regexPattern = quote(D_TESTS_TO_RUN) + "\\S+";
      newParameters = existingParameters.replaceAll(regexPattern, "").trim();
    }

    newParameters = newParameters + " " + D_TESTS_TO_RUN + testFileNames;

    // Trim any duplicate whitespaces
    newParameters = newParameters.replaceAll("\\s+", " ");

    return newParameters;
  }

  /**
   * Returns the executor used to run or debug the test configuration.
   */
  public abstract Executor getExecutor();

  /**
   * Displays an error dialog with the given message.
   *
   * @param errorMessage the error message to display
   */
  public void showErrorDialog(String errorMessage) {
    showMessageDialog(errorMessage, ERROR, Messages.getErrorIcon());
  }
}
