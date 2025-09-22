package ch.postfinance.citrusframework.plugin.action;

import static ch.postfinance.citrusframework.plugin.UserMessages.NO_RUN_CONFIGURATION_SELECTED;
import static ch.postfinance.citrusframework.plugin.action.XmlAbstractAction.PLUGIN_RUN_CONFIGURATION_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.openapi.module.Module;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XmlTestExecuteAbstractActionTest {

  @Mock
  private Executor executorMock;

  @Mock
  private RunManager runManagerMock;

  private XmlTestExecuteAbstractAction fixture;

  @Before
  public void setUp() {
    fixture = new XmlTestExecuteAbstractAction() {
      @Override
      public Executor getExecutor() {
        return executorMock;
      }
    };
  }

  @Test
  public void selectRunConfiguration_throwsError_whenNoConfigurationSelected() {
    doReturn(null).when(runManagerMock).getSelectedConfiguration();

    assertThatThrownBy(() -> fixture.selectRunConfiguration(runManagerMock))
      .asInstanceOf(type(TestInvocationException.class))
      .extracting(TestInvocationException::getDialogMessage)
      .isEqualTo(NO_RUN_CONFIGURATION_SELECTED);
  }

  @Test
  public void selectRunConfiguration_clonesSelectedRunConfiguration() {
    // prepare configuration selection
    RunnerAndConfigurationSettings selectedRunnerAndConfigurationSettingsMock =
      mock();
    doReturn(selectedRunnerAndConfigurationSettingsMock)
      .when(runManagerMock)
      .getSelectedConfiguration();

    // prepare configuration cloning
    ConfigurationFactory configurationFactoryMock = mock();
    doReturn(configurationFactoryMock)
      .when(selectedRunnerAndConfigurationSettingsMock)
      .getFactory();

    RunnerAndConfigurationSettings clonedRunnerAndConfigurationSettingsMock =
      mock();
    doReturn(clonedRunnerAndConfigurationSettingsMock)
      .when(runManagerMock)
      .createConfiguration(
        PLUGIN_RUN_CONFIGURATION_NAME,
        configurationFactoryMock
      );

    // prepare copyClassInformation
    JUnitConfiguration selectedJUnitConfigurationMock = mock();
    doReturn(selectedJUnitConfigurationMock)
      .when(selectedRunnerAndConfigurationSettingsMock)
      .getConfiguration();

    JUnitConfiguration clonedJUnitConfigurationMock = mock();
    doReturn(clonedJUnitConfigurationMock)
      .when(clonedRunnerAndConfigurationSettingsMock)
      .getConfiguration();

    var modules = new Module[] { mock(Module.class), mock(Module.class) };
    doReturn(modules).when(selectedJUnitConfigurationMock).getModules();

    var workingDirectory = "workingDirectory";
    doReturn(workingDirectory)
      .when(selectedJUnitConfigurationMock)
      .getWorkingDirectory();

    JUnitConfiguration.Data persistentDataMock = spy();
    doReturn(persistentDataMock)
      .when(selectedJUnitConfigurationMock)
      .getPersistentData();
    doReturn(persistentDataMock)
      .when(clonedJUnitConfigurationMock)
      .getPersistentData();

    var packageName = "packageName";
    doReturn(packageName).when(persistentDataMock).getPackageName();

    var mainClassName = "mainClassName";
    doReturn(mainClassName).when(persistentDataMock).getMainClassName();

    var methodName = "methodName";
    doReturn(methodName).when(persistentDataMock).getMethodName();

    // prepare copyBeforeRunTasks
    List<BeforeRunTask<?>> beforeRunTasks = List.of(mock(BeforeRunTask.class));
    doReturn(beforeRunTasks)
      .when(selectedJUnitConfigurationMock)
      .getBeforeRunTasks();

    fixture.selectRunConfiguration(runManagerMock);

    verify(clonedJUnitConfigurationMock).setModule(modules[0]);
    verify(clonedJUnitConfigurationMock).setWorkingDirectory(workingDirectory);

    assertThat(persistentDataMock.PACKAGE_NAME).isEqualTo(packageName);
    assertThat(persistentDataMock.MAIN_CLASS_NAME).isEqualTo(mainClassName);
    assertThat(persistentDataMock.METHOD_NAME).isEqualTo(methodName);

    verify(clonedJUnitConfigurationMock).setBeforeRunTasks(beforeRunTasks);

    verify(runManagerMock).addConfiguration(
      clonedRunnerAndConfigurationSettingsMock
    );
  }
}
