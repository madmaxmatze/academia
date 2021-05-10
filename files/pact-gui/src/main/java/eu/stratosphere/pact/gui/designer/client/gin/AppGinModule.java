package eu.stratosphere.pact.gui.designer.client.gin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import eu.stratosphere.pact.gui.designer.client.async.AjaxServiceAsync;
import eu.stratosphere.pact.gui.designer.client.presenter.LoginPresenter.LoginViewInterface;
import eu.stratosphere.pact.gui.designer.client.presenter.MainPresenter.MainViewInterface;
import eu.stratosphere.pact.gui.designer.client.view.LoginView;
import eu.stratosphere.pact.gui.designer.client.view.MainView;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;
import eu.stratosphere.pact.gui.designer.client.i18n.StringConstants;
import eu.stratosphere.pact.gui.designer.client.resources.Images;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListView;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainerManagerView;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainerView;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainerManager.TabContainerManagerViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainer.TabContainerViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramList.ProgramListViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListItem.ProgramListItemViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListItemView;
import eu.stratosphere.pact.gui.designer.client.component.compile_results.CompilerResultView;
import eu.stratosphere.pact.gui.designer.client.component.compile_results.CompilerResult.CompilerResultViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.java_editor.JavaCodeEditorView;
import eu.stratosphere.pact.gui.designer.client.component.java_editor.JavaCodeEditor.JavaCodeEditorViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidgetView;
import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidget.GraphWidgetViewInterface;

/**
 * 
 * Class to configure Gin (GWT Dependency Injection)
 * 
 * Google tutorial: http://code.google.com/p/google-gin/wiki/GinTutorial
 * tutorial 2:
 * http://www.canoo.com/blog/2011/04/05/gwt-dependency-injection-recipes
 * -using-gin/ tutorial 3:
 * http://howtogwt.blogspot.com/2010/03/instance-creation-and-dependency.html
 * scopes:
 * http://code.google.com/docreader/#p=google-guice&s=google-guice&t=Scopes why
 * use guice:
 * http://stackoverflow.com/questions/5271960/pros-and-cons-using-gin-in-gwt
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class AppGinModule extends AbstractGinModule {
	@Override
	protected void configure() {
		bind(EventBus.class).to(CustomEventBus.class).in(Singleton.class); // .asEagerSingleton()
		bind(PactProgramManager.class).in(Singleton.class);
		bind(AjaxServiceAsync.class).in(Singleton.class);
		bind(StringConstants.class).in(Singleton.class);
		bind(Images.class).in(Singleton.class);

		bind(MainViewInterface.class).to(MainView.class);
		bind(LoginViewInterface.class).to(LoginView.class);

		bind(ProgramListViewInterface.class).to(ProgramListView.class);
		bind(ProgramListItemViewInterface.class).to(ProgramListItemView.class);

		bind(TabContainerManagerViewInterface.class).to(TabContainerManagerView.class);
		bind(TabContainerViewInterface.class).to(TabContainerView.class);

		bind(JavaCodeEditorViewInterface.class).to(JavaCodeEditorView.class);
		bind(GraphWidgetViewInterface.class).to(GraphWidgetView.class);
		bind(CompilerResultViewInterface.class).to(CompilerResultView.class);
	}
}