package ch.postfinance.citrusframework.intellij.plugin.action;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultDebugExecutor;

/**
 * This action debugs (Debug) citrus tests selected by the user.
 */
public class XmlTestDebuggerAction extends XmlTestExecuteAbstractAction {

    @Override
    public Executor getExecutor() {
        return DefaultDebugExecutor.getDebugExecutorInstance();
    }
}
