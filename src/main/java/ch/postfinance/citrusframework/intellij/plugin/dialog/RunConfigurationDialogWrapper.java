package ch.postfinance.citrusframework.intellij.plugin.dialog;

import ch.postfinance.citrusframework.intellij.plugin.model.RunConfig;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.Consumer;
import icons.OpenapiIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog displaying run configurations that can be selected by the
 * user for running a citrus test
 */
public class RunConfigurationDialogWrapper extends DialogWrapper {

	public static Map<String, Icon> runConfigIcons = new HashMap<>();

	static {
		runConfigIcons.put("JUnit", AllIcons.RunConfigurations.Junit);
		runConfigIcons.put("Maven", OpenapiIcons.RepositoryLibraryLogo);
	}

	private static final String RUN_DEBUG_CONFIGURATIONS = "Run/Debug Configurations";
	private static final String SELECT_A_CONFIGURATION = "Select a configuration";
	private final JBList<RunConfig> runConfigDescriptorJBList;

	public RunConfigurationDialogWrapper(List<RunConfig> runConfigs) {
		super(true);
		this.runConfigDescriptorJBList = new JBList<>(runConfigs);
		init();
		setTitle(RUN_DEBUG_CONFIGURATIONS);
	}

	@Nullable @Override protected JComponent createCenterPanel() {
		runConfigDescriptorJBList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		runConfigDescriptorJBList.setCellRenderer(SimpleListCellRenderer.create((label, runConfig, index) -> {
			label.setText(runConfig.getName());
			label.setIcon(runConfigIcons.get(runConfig.getType()));
		}));

		ToolbarDecorator decorator = createToolbarDecorator(runConfigDescriptorJBList);
		return LabeledComponent.create(decorator.createPanel(), SELECT_A_CONFIGURATION);
	}

	/**
	 * Show the dialog and execute the callback returning the
	 * selected run configuration if the user click the Ok button
	 *
	 * @param callback the callback
	 */
	public void show(Consumer<RunConfig> callback) {
		if (showAndGet()) {
			callback.consume(runConfigDescriptorJBList.getSelectedValue());
		}
	}

	private ToolbarDecorator createToolbarDecorator(JBList<RunConfig> virtualFileJBList) {
		ToolbarDecorator decorator = ToolbarDecorator.createDecorator(virtualFileJBList);
		decorator.disableAddAction();
		decorator.disableRemoveAction();
		decorator.disableDownAction();
		decorator.disableUpAction();
		return decorator;
	}
}
