package ch.postfinance.citrusframework.plugin.action;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

@RunWith(Enclosed.class)
public class XmlAbstractActionTest {

  public static class Update {

    private XmlAbstractAction fixture;

    @Before
    public void beforeSetup() {
      fixture = new TestXmlAbstractActionImpl();
    }

    @Test
    public void disablesAction_whenNoProject() {
      AnActionEvent event = mock(AnActionEvent.class);
      Presentation presentation = mock(Presentation.class);

      when(event.getProject()).thenReturn(null);
      when(event.getPresentation()).thenReturn(presentation);
      when(event.getData(PlatformCoreDataKeys.SELECTED_ITEMS)).thenReturn(
        new Object[] { mock(VirtualFile.class) }
      );

      fixture.update(event);

      verify(presentation).setEnabledAndVisible(false);
    }

    @Test
    public void enablesAction_whenProjectAndTestFile() {
      AnActionEvent event = mock(AnActionEvent.class);
      Presentation presentation = mock(Presentation.class);
      VirtualFile testFile = mock(VirtualFile.class);

      when(event.getProject()).thenReturn(mock(Project.class)); // non-null project
      when(event.getPresentation()).thenReturn(presentation);
      when(event.getData(PlatformCoreDataKeys.SELECTED_ITEMS)).thenReturn(
        new Object[] { testFile }
      );

      // Mock VirtualFileUtil static method
      try (
        MockedStatic<
          ch.postfinance.citrusframework.plugin.VirtualFileUtil
        > util = mockStatic(
          ch.postfinance.citrusframework.plugin.VirtualFileUtil.class
        )
      ) {
        util
          .when(() ->
            ch.postfinance.citrusframework.plugin.VirtualFileUtil.containsAtLeastOneTestFile(
              any()
            )
          )
          .thenReturn(true);

        fixture.update(event);

        verify(presentation).setEnabledAndVisible(true);
      }
    }
  }

  public static class ShowErrorDialog {

    private XmlAbstractAction fixture;

    @Before
    public void beforeSetup() {
      fixture = new TestXmlAbstractActionImpl();
    }

    @Test
    public void showErrorDialog_invokesMessageDialog() {
      try (MockedStatic<Messages> messages = mockStatic(Messages.class)) {
        fixture.showErrorDialog("Something went wrong");

        messages.verify(() ->
          Messages.showMessageDialog(
            eq("Something went wrong"),
            eq(XmlAbstractAction.ERROR),
            any()
          )
        );
      }
    }
  }

  private static class TestXmlAbstractActionImpl extends XmlAbstractAction {

    private final AtomicBoolean called = new AtomicBoolean(false);

    public boolean hasBeenCalled() {
      return called.get();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
      called.set(true);
    }
  }
}
