package eu.stratosphere.pact.gui.designer.client.component.tab;

import java.util.Date;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.TabItemConfig;

import eu.stratosphere.pact.gui.designer.client.component.compile_results.CompilerResult;
import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidget;
import eu.stratosphere.pact.gui.designer.client.component.java_editor.JavaCodeEditor;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;

/**
 * single tabs added to a tab panel are wrapped into this TabItem class, to be
 * able to add more logic
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class TabItem {
	private PactProgramCompilerResult compilationResult = null;
	private Pact javaCodeEditorPact = null;
	private PactProgram grapghDesignerPactProgram = null;

	private CompilerResult compilerResult = null;
	private JavaCodeEditor javaCodeEditor = null;
	private GraphWidget graphWidget = null;

	private AppInjector appInjector = null;

	/**
	 * Constructor for a PactProgramCompilerResult-Tab
	 * 
	 * @param appInjector
	 * @param result
	 */
	public TabItem(AppInjector appInjector, PactProgramCompilerResult result) {
		this.appInjector = appInjector;
		this.compilationResult = result;
		itemType = TabItemType.COMPILER_RESULTS;
	}

	/**
	 * Constructor for a Pact code editor-Tab
	 * 
	 * @param appInjector
	 * @param pact
	 */
	public TabItem(AppInjector appInjector, Pact pact) {
		this.appInjector = appInjector;
		this.javaCodeEditorPact = pact;
		itemType = TabItemType.JAVA;
	}

	/**
	 * Constructor for a PactProgram-Graph-Tab
	 * 
	 * @param appInjector
	 * @param pactProgram
	 */
	public TabItem(AppInjector appInjector, PactProgram pactProgram) {
		this.appInjector = appInjector;
		this.grapghDesignerPactProgram = pactProgram;
		itemType = TabItemType.GRAPH;
	}

	/**
	 * id should just be unique
	 * 
	 * @return
	 */
	String id = null;

	public String getId() {
		if (id == null) {
			switch (itemType) {
			case JAVA:
				id = String.valueOf(javaCodeEditorPact.getId());
				break;
			case COMPILER_RESULTS:
				id = (new Date()).getTime() + "" + Math.random();
				break;
			case GRAPH:
				id = "graph";
				break;
			default:
				Log.error("unknown item type");
				break;
			}
		}
		return id;
	}

	private Widget widget = null;

	/**
	 * TabItem is also kind of a presenter, wrapping COMPILER_RESULTS, JAVA and
	 * GRAPH presenter. This method returnes the child presenters view
	 */
	public Widget getView() {
		if (widget == null) {
			switch (itemType) {
			case COMPILER_RESULTS:
				compilerResult = new CompilerResult(appInjector, compilationResult);
				widget = (Widget) compilerResult.getView();
				break;
			case JAVA:
				javaCodeEditor = new JavaCodeEditor(appInjector, javaCodeEditorPact);
				widget = (Widget) javaCodeEditor.getView();
				break;
			case GRAPH:
				graphWidget = new GraphWidget(appInjector, grapghDesignerPactProgram);
				widget = (Widget) graphWidget.getView();
				break;
			}
		}

		return widget;
	}

	/**
	 * only these kinds of tabs are allowed
	 * 
	 * @author MathiasNitzsche@gmail.com
	 */
	enum TabItemType {
		JAVA, COMPILER_RESULTS, GRAPH
	}

	public TabItemType itemType = TabItemType.JAVA;

	public TabItemType getType() {
		return itemType;
	}

	/**
	 * Create tab config object, needed when adding new tabs for graph, java,
	 * compilation
	 * 
	 * @param tabLabel
	 * @param closeable
	 * @param icon
	 * @return
	 */
	public TabItemConfig getConfig() {
		String tabLabel = "";
		boolean closeable = true;
		ImageResource icon = appInjector.getImages().pact16();

		switch (itemType) {
		case JAVA:
			tabLabel = javaCodeEditorPact.getType().getLabel().toUpperCase() + ": " + javaCodeEditorPact.getName();
			break;
		case COMPILER_RESULTS:
			tabLabel = "Compile Log: " + (new Date());
			icon = (compilerResult.getResult().wasSuccessFul() ? appInjector.getImages().log_ok16() : appInjector
					.getImages().log_error16());
			break;
		case GRAPH:
			tabLabel = "Graph: " + grapghDesignerPactProgram.getName();
			closeable = false;
			icon = appInjector.getImages().graph16();
			break;
		}

		TabItemConfig tabItemConfig = new TabItemConfig();
		tabItemConfig.setClosable(closeable);
		tabItemConfig.setHTML(tabLabel);
		tabItemConfig.setIcon(icon);
		return tabItemConfig;
	}

	public String toString() {
		String label = "TabItem for ";

		switch (itemType) {
		case JAVA:
			label += javaCodeEditorPact;
			break;
		case COMPILER_RESULTS:
			label += compilerResult;
			break;
		case GRAPH:
			label += "Graph: " + grapghDesignerPactProgram.getName();
			break;
		}
		return label;
	}
}
