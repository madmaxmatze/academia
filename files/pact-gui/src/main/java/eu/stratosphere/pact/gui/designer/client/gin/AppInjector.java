package eu.stratosphere.pact.gui.designer.client.gin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

import eu.stratosphere.pact.gui.designer.client.async.AjaxServiceAsync;
import eu.stratosphere.pact.gui.designer.client.component.compile_results.CompilerResult.CompilerResultViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramList.ProgramListViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListItem.ProgramListItemViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainer.TabContainerViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainerManager.TabContainerManagerViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.java_editor.JavaCodeEditor.JavaCodeEditorViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidget.GraphWidgetViewInterface;
import eu.stratosphere.pact.gui.designer.client.i18n.StringConstants;
import eu.stratosphere.pact.gui.designer.client.presenter.LoginPresenter.LoginViewInterface;
import eu.stratosphere.pact.gui.designer.client.presenter.MainPresenter.MainViewInterface;
import eu.stratosphere.pact.gui.designer.client.resources.Images;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;

/**
 * Interface defining objects available over Gin (GWT Dependency Injection)
 * Either parameters or return values allowed:
 * http://groups.google.com/group/google
 * -gin/browse_thread/thread/28209561928d0968
 * 
 * @author MathiasNitzsche@gmail.com
 */
@GinModules(AppGinModule.class)
public interface AppInjector extends Ginjector {
	EventBus getEventBus();

	AjaxServiceAsync getAjaxService();

	PactProgramManager getPactProgramManager();

	MainViewInterface getMainView();

	LoginViewInterface getLoginView();

	ProgramListViewInterface getProgramListView();

	ProgramListItemViewInterface getProgramListItemView();

	TabContainerManagerViewInterface getTabContainerManagerView();

	TabContainerViewInterface getTabContainerView();

	StringConstants i18n();

	Images getImages();

	CompilerResultViewInterface getCompilerResultView();

	JavaCodeEditorViewInterface getJavaCodeEditorView();

	GraphWidgetViewInterface getGraphWidgetView();
}