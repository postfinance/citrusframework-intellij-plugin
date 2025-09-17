package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.INVALID_RUN_CONFIGURATION;
import static ch.postfinance.citrusframework.plugin.UserMessages.NO_RUN_CONFIGURATION_SELECTED;
import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE_ARRAY;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import ch.postfinance.citrusframework.plugin.VirtualFileUtil;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Factory;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

@RunWith(Enclosed.class)
public class XmlTestExecuteAbstractActionTest {

  public static class ActionPerformed {

    private XmlTestExecuteAbstractAction fixture;

    private AnActionEvent event;
    private Project project;
    private RunManager runManager;
    private Factory<RunnerAndConfigurationSettings> factory;
    private RunnerAndConfigurationSettings selectedConfig;
    private JavaTestConfigurationBase javaTestConfig;
    private Executor executor;

    @Before
    public void setUp() {
      // Anonymous subclass to provide Executor
      executor = mock(Executor.class);
      fixture = new XmlTestExecuteAbstractAction() {
        @Override
        public Executor getExecutor() {
          return executor;
        }
      };

      event = mock(AnActionEvent.class);
      project = mock(Project.class);
      runManager = mock(RunManager.class);
      factory = mock(Factory.class);
      selectedConfig = mock(RunnerAndConfigurationSettings.class);
      javaTestConfig = mock(JavaTestConfigurationBase.class);
    }

    @Test
    public void showsError_whenNoProject() {
      // project is null
      doReturn(null).when(event).getProject();

      try (MockedStatic<Messages> messages = mockStatic(Messages.class)) {
        assertThatCode(() ->
          fixture.actionPerformed(event)
        ).doesNotThrowAnyException();

        messages.verify(() ->
          Messages.showMessageDialog(
            eq("Project not found."),
            eq(XmlAbstractAction.ERROR),
            any()
          )
        );
      }
    }

    @Test
    public void showsError_whenNoExistingConfiguration() {
      doReturn(project).when(event).getProject();

      try (MockedStatic<RunManager> runManager = mockStatic(RunManager.class)) {
        runManager
          .when(() -> RunManager.getInstance(project))
          .thenReturn(this.runManager);
        doReturn(null).when(this.runManager).getSelectedConfiguration();

        try (
          MockedStatic<VirtualFileUtil> virtualFileUtil = mockStatic(
            VirtualFileUtil.class
          );
          MockedStatic<Messages> messages = mockStatic(Messages.class)
        ) {
          virtualFileUtil
            .when(() -> VirtualFileUtil.retrieveTestFileNames(any()))
            .thenReturn("*Test*");

          assertThatCode(() ->
            fixture.actionPerformed(event)
          ).doesNotThrowAnyException();

          messages.verify(() ->
            Messages.showMessageDialog(
              eq(NO_RUN_CONFIGURATION_SELECTED),
              eq(XmlAbstractAction.ERROR),
              any()
            )
          );
        }
      }
    }

    @Test
    public void showsError_whenInvalidExistingConfiguration() {
      doReturn(project).when(event).getProject();

      try (MockedStatic<RunManager> runManager = mockStatic(RunManager.class)) {
        runManager
          .when(() -> RunManager.getInstance(project))
          .thenReturn(this.runManager);
        doReturn(mock(RunnerAndConfigurationSettings.class))
          .when(this.runManager)
          .getSelectedConfiguration();

        try (
          MockedStatic<VirtualFileUtil> virtualFileUtil = mockStatic(
            VirtualFileUtil.class
          );
          MockedStatic<Messages> messages = mockStatic(Messages.class)
        ) {
          virtualFileUtil
            .when(() -> VirtualFileUtil.retrieveTestFileNames(any()))
            .thenReturn("*Test*");

          assertThatCode(() ->
            fixture.actionPerformed(event)
          ).doesNotThrowAnyException();

          messages.verify(() ->
            Messages.showMessageDialog(
              eq(INVALID_RUN_CONFIGURATION),
              eq(XmlAbstractAction.ERROR),
              any()
            )
          );
        }
      }
    }

    @Test
    public void runsConfiguration_whenValidSetup() {
      doReturn(project).when(event).getProject();

      VirtualFile mockFile = mock();
      doReturn(new VirtualFile[] { mockFile })
        .when(event)
        .getData(VIRTUAL_FILE_ARRAY);

      try (MockedStatic<RunManager> runManager = mockStatic(RunManager.class)) {
        runManager
          .when(() -> RunManager.getInstance(project))
          .thenReturn(this.runManager);
        doReturn(selectedConfig)
          .when(this.runManager)
          .getSelectedConfiguration();

        doReturn(javaTestConfig).when(selectedConfig).getConfiguration();
        doReturn("origName").when(selectedConfig).getName();

        doReturn(factory).when(selectedConfig).createFactory();

        RunnerAndConfigurationSettings clonedSettings = mock();
        doReturn(clonedSettings).when(factory).create();

        RunConfiguration clonedRunConfiguration = mock();
        doReturn(clonedRunConfiguration)
          .when(clonedSettings)
          .getConfiguration();

        try (
          MockedStatic<VirtualFileUtil> virtualFileUtil = mockStatic(
            VirtualFileUtil.class
          )
        ) {
          virtualFileUtil
            .when(() -> VirtualFileUtil.retrieveTestFileNames(any()))
            .thenReturn("*Test*");

          try (
            MockedStatic<ProgramRunnerUtil> runnerUtil = mockStatic(
              ProgramRunnerUtil.class
            )
          ) {
            fixture.actionPerformed(event);

            verify(clonedRunConfiguration).setBeforeRunTasks(anyList());

            verify(javaTestConfig).setName("origName: *Test*");
            verify(javaTestConfig).setVMParameters("-Dtests.to.run=*Test*");

            runnerUtil.verify(() ->
              ProgramRunnerUtil.executeConfiguration(
                eq(clonedSettings),
                eq(executor)
              )
            );
          }
        }
      }
    }
  }
}
