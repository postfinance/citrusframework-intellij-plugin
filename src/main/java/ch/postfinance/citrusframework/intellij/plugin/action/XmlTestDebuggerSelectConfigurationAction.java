package ch.postfinance.citrusframework.intellij.plugin.action;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultDebugExecutor;

/**
 * This action debugs (Debug) citrus tests after the user selected a run configuration file.
 */
public class XmlTestDebuggerSelectConfigurationAction
  extends XmlTestSelectConfigurationAbstractAction {

  @Override
  public Executor getExecutor() {
    return DefaultDebugExecutor.getDebugExecutorInstance();
  }
}
