package ch.postfinance.citrusframework.plugin.action;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExtractConfigTypeTest {

  @Test
  void extracts_typeBeforeDot() {
    assertThat(
      XmlTestSelectConfigurationAbstractAction.extractConfigType(
        "JUnit.myConfig"
      )
    ).isEqualTo("JUnit");
  }

  @Test
  void extracts_typeBeforeFirstDot() {
    assertThat(
      XmlTestSelectConfigurationAbstractAction.extractConfigType(
        "Maven.my.dotted.config"
      )
    ).isEqualTo("Maven");
  }

  @Test
  void returns_fullString_whenNoDotPresent() {
    assertThat(
      XmlTestSelectConfigurationAbstractAction.extractConfigType("noDotsHere")
    ).isEqualTo("noDotsHere");
  }

  @Test
  void returns_empty_forLeadingDot() {
    assertThat(
      XmlTestSelectConfigurationAbstractAction.extractConfigType(".leadingDot")
    ).isEmpty();
  }

  @Test
  void returns_empty_forSingleDot() {
    assertThat(
      XmlTestSelectConfigurationAbstractAction.extractConfigType(".")
    ).isEmpty();
  }
}
