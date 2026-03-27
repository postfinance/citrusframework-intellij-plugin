package ch.postfinance.citrusframework.plugin.dialog;

import static org.assertj.core.api.Assertions.assertThat;

import ch.postfinance.citrusframework.plugin.model.RunConfig;
import org.junit.jupiter.api.Test;

class RunConfigurationDialogWrapperTest {

  @Test
  void runConfig_stores_nameAndType() {
    RunConfig runConfig = new RunConfig("myConfig", "JUnit");

    assertThat(runConfig.getName()).isEqualTo("myConfig");
    assertThat(runConfig.getType()).isEqualTo("JUnit");
  }
}
