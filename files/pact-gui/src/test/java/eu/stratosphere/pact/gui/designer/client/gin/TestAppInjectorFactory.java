package eu.stratosphere.pact.gui.designer.client.gin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import eu.stratosphere.pact.gui.designer.client.component.compile_results.CompilerResult.CompilerResultViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidget.GraphWidgetViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.java_editor.JavaCodeEditor.JavaCodeEditorViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramList.ProgramListViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListItem.ProgramListItemViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainer.TabContainerViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainerManager.TabContainerManagerViewInterface;
import eu.stratosphere.pact.gui.designer.client.i18n.StringConstants;
import eu.stratosphere.pact.gui.designer.client.presenter.LoginPresenter;
import eu.stratosphere.pact.gui.designer.client.presenter.LoginPresenter.LoginViewInterface;
import eu.stratosphere.pact.gui.designer.client.presenter.MainPresenter;
import eu.stratosphere.pact.gui.designer.client.presenter.MainPresenter.MainViewInterface;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;

public class TestAppInjectorFactory {
	public static AppInjector getNewAppInjectorForTest() {
		// mock AppInjector
		AppInjector appInjectorMock = mock(AppInjector.class);

		// create REAL EventBus
		CustomEventBus customEventBus = new CustomEventBus();
		when(appInjectorMock.getEventBus()).thenReturn(customEventBus);

		// create REAL PactProgramManager
		PactProgramManager pactProgramManager = new PactProgramManager(appInjectorMock);
		when(appInjectorMock.getPactProgramManager()).thenReturn(pactProgramManager);

		// mock StringConstants (i18n)
		StringConstants constantMock = mock(StringConstants.class);
		when(appInjectorMock.i18n()).thenReturn(constantMock);

		// mock MainView
		MainViewInterface mainViewMock = mock(MainPresenter.MainViewInterface.class);
		when(appInjectorMock.getMainView()).thenReturn(mainViewMock);

		// mock LoginView
		LoginViewInterface loginViewMock = mock(LoginPresenter.LoginViewInterface.class);
		when(appInjectorMock.getLoginView()).thenReturn(loginViewMock);

		// mock ProgramListView
		ProgramListViewInterface programListViewMock = mock(ProgramListViewInterface.class);
		when(appInjectorMock.getProgramListView()).thenReturn(programListViewMock);

		// mock ProgramListItemView
		ProgramListItemViewInterface programListItemViewMock = mock(ProgramListItemViewInterface.class);
		when(appInjectorMock.getProgramListItemView()).thenReturn(programListItemViewMock);

		// mock TabContainerManagerView
		TabContainerManagerViewInterface tabContainerManagerViewMock = mock(TabContainerManagerViewInterface.class);
		when(appInjectorMock.getTabContainerManagerView()).thenReturn(tabContainerManagerViewMock);

		// mock TabContainerView
		TabContainerViewInterface tabContainerViewMock = mock(TabContainerViewInterface.class);
		when(appInjectorMock.getTabContainerView()).thenReturn(tabContainerViewMock);

		// mock CompilerResultView
		CompilerResultViewInterface compilerResultViewMock = mock(CompilerResultViewInterface.class);
		when(appInjectorMock.getCompilerResultView()).thenReturn(compilerResultViewMock);

		// mock JavaCodeEditorView
		JavaCodeEditorViewInterface javaCodeEditorViewMock = mock(JavaCodeEditorViewInterface.class);
		when(appInjectorMock.getJavaCodeEditorView()).thenReturn(javaCodeEditorViewMock);

		// mock GraphWidgetView
		GraphWidgetViewInterface graphWidgetViewMock = mock(GraphWidgetViewInterface.class);
		when(appInjectorMock.getGraphWidgetView()).thenReturn(graphWidgetViewMock);

		return appInjectorMock;
	}
}
