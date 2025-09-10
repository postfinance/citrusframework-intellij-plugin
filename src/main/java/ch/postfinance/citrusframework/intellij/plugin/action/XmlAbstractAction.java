package ch.postfinance.citrusframework.intellij.plugin.action;

import static ch.postfinance.citrusframework.intellij.plugin.VirtualFileUtil.containsAtLeastOneTestFile;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import static java.util.Objects.nonNull;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class XmlAbstractAction extends AnAction {

  protected static final String ERROR = "Error";

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

  public void showErrorDialog(String errorMessage) {
    showMessageDialog(errorMessage, ERROR, Messages.getErrorIcon());
  }
}
