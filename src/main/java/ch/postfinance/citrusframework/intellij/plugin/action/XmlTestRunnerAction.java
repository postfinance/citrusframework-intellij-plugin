package ch.postfinance.citrusframework.intellij.plugin.action;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;

/**
 * This action runs (Run) citrus tests selected by the user.
 */
public class XmlTestRunnerAction extends XmlTestExecuteAbstractAction {

    @Override
    public Executor getExecutor() {
        return DefaultRunExecutor.getRunExecutorInstance();
    }
}
