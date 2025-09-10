package ch.postfinance.citrusframework.plugin.action;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;

/**
 * This action runs (Run) citrus tests after the user selected a run configuration file.
 */
public class XmlTestRunnerSelectConfigurationAction
  extends XmlTestSelectConfigurationAbstractAction {

  @Override
  public Executor getExecutor() {
    return DefaultRunExecutor.getRunExecutorInstance();
  }
}
