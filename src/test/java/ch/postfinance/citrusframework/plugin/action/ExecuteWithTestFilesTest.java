package ch.postfinance.citrusframework.plugin.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.util.Factory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecuteWithTestFilesTest {

  @Nested
  class ConfigurationCopySetup {

    @Test
    void sets_vmParameters_onCopy_withExistingParams() {
      String result = buildVmParameters("-Xmx512m", "*MyTest*");

      assertThat(result).isEqualTo("-Xmx512m -Dtests.to.run=*MyTest*");
    }

    @Test
    void sets_vmParameters_onCopy_withNullExistingParams() {
      String result = buildVmParameters(null, "*Test*");

      assertThat(result).isEqualTo("-Dtests.to.run=*Test*");
    }

    @Test
    void sets_vmParameters_onCopy_withEmptyExistingParams() {
      String result = buildVmParameters("", "*MyTest*");

      assertThat(result).isEqualTo(" -Dtests.to.run=*MyTest*");
    }

    @Test
    void createFactory_produces_independentCopy(
      @Mock RunnerAndConfigurationSettings selectedConfigurationMock,
      @Mock RunnerAndConfigurationSettings copyConfigSettingsMock,
      @Mock JavaTestConfigurationBase copyConfigMock
    ) {
      Factory<RunnerAndConfigurationSettings> factory = () ->
        copyConfigSettingsMock;
      when(selectedConfigurationMock.createFactory()).thenReturn(factory);
      when(copyConfigSettingsMock.getConfiguration()).thenReturn(
        copyConfigMock
      );

      RunnerAndConfigurationSettings copy = selectedConfigurationMock
        .createFactory()
        .create();

      assertThat(copy)
        .isSameAs(copyConfigSettingsMock)
        .isNotSameAs(selectedConfigurationMock);

      assertThat(copy.getConfiguration()).isSameAs(copyConfigMock);
    }

    private String buildVmParameters(String existing, String testFileNames) {
      String dTestsToRun = "-Dtests.to.run=";
      if (existing != null) {
        return existing + " " + dTestsToRun + testFileNames;
      }
      return dTestsToRun + testFileNames;
    }
  }

  @Nested
  class ConfigurationTypeCheck {

    @Mock
    private RunnerAndConfigurationSettings selectedConfigurationMock;

    @Test
    void accepts_javaTestConfigurationBase(
      @Mock JavaTestConfigurationBase javaTestConfigMock
    ) {
      when(selectedConfigurationMock.getConfiguration()).thenReturn(
        javaTestConfigMock
      );

      assertThat(selectedConfigurationMock.getConfiguration()).isInstanceOf(
        JavaTestConfigurationBase.class
      );
    }

    @Test
    void rejects_nonJavaTestConfiguration(
      @Mock RunConfiguration nonJavaConfigMock,
      @Mock ConfigurationType configTypeMock
    ) {
      when(selectedConfigurationMock.getConfiguration()).thenReturn(
        nonJavaConfigMock
      );
      when(nonJavaConfigMock.getType()).thenReturn(configTypeMock);
      when(configTypeMock.getDisplayName()).thenReturn("Application");

      assertThat(selectedConfigurationMock.getConfiguration()).isNotInstanceOf(
        JavaTestConfigurationBase.class
      );
      assertThat(nonJavaConfigMock.getType().getDisplayName()).isEqualTo(
        "Application"
      );
    }
  }
}
