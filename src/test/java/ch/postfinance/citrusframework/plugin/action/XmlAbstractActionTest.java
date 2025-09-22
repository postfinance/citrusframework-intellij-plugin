package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.INVALID_RUN_CONFIGURATION;
import static ch.postfinance.citrusframework.plugin.UserMessages.PROJECT_NOT_FOUND;
import static ch.postfinance.citrusframework.plugin.action.XmlAbstractAction.PLUGIN_RUN_CONFIGURATION_NAME;
import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE_ARRAY;
import static com.intellij.openapi.actionSystem.PlatformCoreDataKeys.SELECTED_ITEMS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import ch.postfinance.citrusframework.plugin.VirtualFileUtil;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XmlAbstractActionTest {

  @Mock
  private RunnerAndConfigurationSettings selectedRunnerAndConfigurationSettingsMock;

  @Mock
  private Executor executorMock;

  @Mock
  private Project projectMock;

  @Mock
  private RunManager runManagerMock;

  private AutoCloseable mocks;

  private XmlAbstractAction fixture;

  @Before
  public void setUp() {
    mocks = openMocks(this);
    fixture = new TestXmlAbstractActionImpl();
  }

  @After
  public void teardown() throws Exception {
    mocks.close();
  }

  @Test
  public void update_disablesAction_whenNoProject() {
    AnActionEvent event = mock(AnActionEvent.class);
    Presentation presentation = mock(Presentation.class);

    when(event.getProject()).thenReturn(null);
    when(event.getPresentation()).thenReturn(presentation);
    when(event.getData(SELECTED_ITEMS)).thenReturn(
      new Object[] { mock(VirtualFile.class) }
    );

    fixture.update(event);

    verify(presentation).setEnabledAndVisible(false);
  }

  @Test
  public void update_enablesAction_whenProjectAndTestFile() {
    AnActionEvent event = mock(AnActionEvent.class);
    Presentation presentation = mock(Presentation.class);
    VirtualFile testFile = mock(VirtualFile.class);

    when(event.getProject()).thenReturn(mock(Project.class)); // non-null project
    when(event.getPresentation()).thenReturn(presentation);
    when(event.getData(SELECTED_ITEMS)).thenReturn(new Object[] { testFile });

    try (
      MockedStatic<VirtualFileUtil> virtualFileUtil = mockStatic(
        VirtualFileUtil.class
      )
    ) {
      virtualFileUtil
        .when(() -> VirtualFileUtil.containsAtLeastOneTestFile(any()))
        .thenReturn(true);

      fixture.update(event);

      verify(presentation).setEnabledAndVisible(true);
    }
  }

  @Test
  public void actionPerformed_invokesErrorDialog_whenNoProjectSelected() {
    AnActionEvent actionEvent = mock();
    doReturn(null).when(actionEvent).getProject();

    try (MockedStatic<Messages> messages = mockStatic(Messages.class)) {
      fixture.actionPerformed(actionEvent);

      messages.verify(() ->
        Messages.showMessageDialog(
          eq(PROJECT_NOT_FOUND),
          eq(XmlAbstractAction.ERROR),
          any()
        )
      );
    }
  }

  private void withMockedRunManager(
    BiConsumer<AnActionEvent, RunManager> consumer
  ) {
    AnActionEvent actionEventMock = mock();
    doReturn(projectMock).when(actionEventMock).getProject();

    try (MockedStatic<RunManager> runManager = mockStatic(RunManager.class)) {
      runManager
        .when(() -> RunManager.getInstance(projectMock))
        .thenReturn(runManagerMock);
      consumer.accept(actionEventMock, runManagerMock);
    }
  }

  private static RunnerAndConfigurationSettings mockPredefinedRunnerAndConfigurationSettings(
    RunManager runManagerMock
  ) {
    RunnerAndConfigurationSettings runnerAndConfigurationSettingsMock = mock();
    doReturn(PLUGIN_RUN_CONFIGURATION_NAME)
      .when(runnerAndConfigurationSettingsMock)
      .getName();

    doReturn(singletonList(runnerAndConfigurationSettingsMock))
      .when(runManagerMock)
      .getAllSettings();

    return runnerAndConfigurationSettingsMock;
  }

  void withMockedVirtualFileUtil(
    AnActionEvent actionEventMock,
    Consumer<MockedStatic<VirtualFileUtil>> consumer
  ) {
    VirtualFile[] virtualFiles = new VirtualFile[0];
    doReturn(virtualFiles).when(actionEventMock).getData(VIRTUAL_FILE_ARRAY);

    try (
      MockedStatic<VirtualFileUtil> virtualFileUtil = mockStatic(
        VirtualFileUtil.class
      )
    ) {
      virtualFileUtil
        .when(() -> VirtualFileUtil.retrieveTestFileNames(virtualFiles))
        .thenReturn("*Test*");
      consumer.accept(virtualFileUtil);
    }
  }

  void assertActionPerformedWithRunnerUtil(
    RunnerAndConfigurationSettings runnerAndConfigurationSettingsMock,
    Runnable runnable
  ) {
    try (
      MockedStatic<ProgramRunnerUtil> runnerUtil = mockStatic(
        ProgramRunnerUtil.class
      )
    ) {
      runnable.run();

      runnerUtil.verify(() ->
        ProgramRunnerUtil.executeConfiguration(
          eq(runnerAndConfigurationSettingsMock),
          eq(executorMock)
        )
      );
    }
  }

  private static void assertThatRunConfigurationWasSelected(
    RunManager runManagerMock,
    RunnerAndConfigurationSettings runnerAndConfigurationSettingsMock
  ) {
    verify(runManagerMock).getAllSettings();
    verify(runManagerMock).setSelectedConfiguration(
      runnerAndConfigurationSettingsMock
    );
    verifyNoMoreInteractions(runManagerMock);
  }

  @Test
  public void actionPerformed_selectsExistingActionFirst() {
    withMockedRunManager((actionEventMock, runManagerMock) -> {
      RunnerAndConfigurationSettings runnerAndConfigurationSettingsMock =
        mockPredefinedRunnerAndConfigurationSettings(runManagerMock);

      JavaTestConfigurationBase javaTestConfigurationBaseMock = mock();
      doReturn(javaTestConfigurationBaseMock)
        .when(runnerAndConfigurationSettingsMock)
        .getConfiguration();

      withMockedVirtualFileUtil(actionEventMock, virtualFileUtil ->
        assertActionPerformedWithRunnerUtil(
          runnerAndConfigurationSettingsMock,
          () -> {
            fixture.actionPerformed(actionEventMock);

            verify(javaTestConfigurationBaseMock).setVMParameters(
              "-Dtests.to.run=*Test*"
            );

            assertThatRunConfigurationWasSelected(
              runManagerMock,
              runnerAndConfigurationSettingsMock
            );
          }
        )
      );
    });
  }

  @Test
  public void actionPerformed_selectsExistingActionFirst_andPerformsJavaTypeCheck() {
    withMockedRunManager((actionEventMock, runManagerMock) -> {
      RunnerAndConfigurationSettings runnerAndConfigurationSettingsMock =
        mockPredefinedRunnerAndConfigurationSettings(runManagerMock);

      RunConfiguration runConfigurationMock = mock();
      doReturn(runConfigurationMock)
        .when(runnerAndConfigurationSettingsMock)
        .getConfiguration();

      try (
        MockedStatic<VirtualFileUtil> virtualFileUtil = mockStatic(
          VirtualFileUtil.class
        )
      ) {
        try (
          MockedStatic<ProgramRunnerUtil> runnerUtil = mockStatic(
            ProgramRunnerUtil.class
          )
        ) {
          try (MockedStatic<Messages> messages = mockStatic(Messages.class)) {
            fixture.actionPerformed(actionEventMock);

            messages.verify(() ->
              Messages.showMessageDialog(
                eq(INVALID_RUN_CONFIGURATION),
                eq(XmlAbstractAction.ERROR),
                any()
              )
            );
          }

          verify(runManagerMock).getAllSettings();
          verifyNoMoreInteractions(runManagerMock);

          virtualFileUtil.verifyNoInteractions();
          runnerUtil.verifyNoInteractions();
        }
      }
    });
  }

  @Test
  public void actionPerformed_respectsExistingVmParameters() {
    withMockedRunManager((actionEventMock, runManagerMock) -> {
      RunnerAndConfigurationSettings runnerAndConfigurationSettingsMock =
        mockPredefinedRunnerAndConfigurationSettings(runManagerMock);

      JavaTestConfigurationBase javaTestConfigurationBaseMock = mock();
      doReturn(javaTestConfigurationBaseMock)
        .when(runnerAndConfigurationSettingsMock)
        .getConfiguration();
      doReturn("  existing vm parameters  ")
        .when(javaTestConfigurationBaseMock)
        .getVMParameters();

      withMockedVirtualFileUtil(actionEventMock, virtualFileUtil ->
        assertActionPerformedWithRunnerUtil(
          runnerAndConfigurationSettingsMock,
          () -> {
            fixture.actionPerformed(actionEventMock);

            verify(javaTestConfigurationBaseMock).setVMParameters(
              "existing vm parameters -Dtests.to.run=*Test*"
            );

            assertThatRunConfigurationWasSelected(
              runManagerMock,
              runnerAndConfigurationSettingsMock
            );
          }
        )
      );
    });
  }

  @Test
  public void actionPerformed_replacesExistingTestFilter() {
    withMockedRunManager((actionEventMock, runManagerMock) -> {
      RunnerAndConfigurationSettings runnerAndConfigurationSettingsMock =
        mockPredefinedRunnerAndConfigurationSettings(runManagerMock);

      JavaTestConfigurationBase javaTestConfigurationBaseMock = mock();
      doReturn(javaTestConfigurationBaseMock)
        .when(runnerAndConfigurationSettingsMock)
        .getConfiguration();
      doReturn("-Dtests.to.run=*preselection*")
        .when(javaTestConfigurationBaseMock)
        .getVMParameters();

      withMockedVirtualFileUtil(actionEventMock, virtualFileUtil ->
        assertActionPerformedWithRunnerUtil(
          runnerAndConfigurationSettingsMock,
          () -> {
            fixture.actionPerformed(actionEventMock);

            verify(javaTestConfigurationBaseMock).setVMParameters(
              "-Dtests.to.run=*Test*"
            );

            assertThatRunConfigurationWasSelected(
              runManagerMock,
              runnerAndConfigurationSettingsMock
            );
          }
        )
      );
    });
  }

  @Test
  public void actionPerformed_resolvesSelectedConfiguration_ifDefaultConfigNotPresent() {
    withMockedRunManager((actionEventMock, runManagerMock) -> {
      doReturn(emptyList()).when(runManagerMock).getAllSettings();

      JavaTestConfigurationBase javaTestConfigurationBaseMock = mock();
      doReturn(javaTestConfigurationBaseMock)
        .when(selectedRunnerAndConfigurationSettingsMock)
        .getConfiguration();

      withMockedVirtualFileUtil(actionEventMock, virtualFileUtil ->
        assertActionPerformedWithRunnerUtil(
          selectedRunnerAndConfigurationSettingsMock,
          () -> {
            fixture.actionPerformed(actionEventMock);

            verify(javaTestConfigurationBaseMock).setVMParameters(
              "-Dtests.to.run=*Test*"
            );

            assertThatRunConfigurationWasSelected(
              runManagerMock,
              selectedRunnerAndConfigurationSettingsMock
            );
          }
        )
      );
    });
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

  private class TestXmlAbstractActionImpl extends XmlAbstractAction {

    @Override
    protected @NotNull RunnerAndConfigurationSettings selectRunConfiguration(
      RunManager runManager
    ) throws TestInvocationException {
      return selectedRunnerAndConfigurationSettingsMock;
    }

    @Override
    public Executor getExecutor() {
      return executorMock;
    }
  }
}
