package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.VirtualFileUtil.containsAtLeastOneTestFile;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base action for operations on Citrus XML test files.
 * <p>
 * Provides:
 * <ul>
 *   <li>Automatic enable/disable visibility depending on project context and selected files</li>
 *   <li>Utility method for showing error dialogs</li>
 * </ul>
 */
public abstract class XmlAbstractAction extends AnAction {

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
    var dataItems = event.getData(PlatformCoreDataKeys.SELECTED_ITEMS);
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

  /**
   * Displays an error dialog with the given message.
   *
   * @param errorMessage the error message to display
   */
  public void showErrorDialog(String errorMessage) {
    showMessageDialog(errorMessage, ERROR, Messages.getErrorIcon());
  }
}
