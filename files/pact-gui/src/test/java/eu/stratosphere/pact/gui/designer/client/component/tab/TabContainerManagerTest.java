package eu.stratosphere.pact.gui.designer.client.component.tab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;

public class TabContainerManagerTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	@Before
	public void setUp() {
	}

	@Test
	public void test() {
		// create MainPresenter (only for easy program creation and deletion)
		// MainPresenter mainPresenter = new MainPresenter(appInjectorMock,
		// appInjectorMock.getMainView());
		// assertNotNull(mainPresenter);

		// create ProgramList
		TabContainerManager tabContainerManager = new TabContainerManager(appInjectorMock);
		assertNotNull(tabContainerManager);

		// test - no programs existing
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 0);
		assertEquals(tabContainerManager.getTabContainers().size(), 0);

		// add PactProgram to model (via main presenterMenu)
		// then check if item was added to ProgramList
		PactProgram pactProgram = null;
		try {
			pactProgram = appInjectorMock.getPactProgramManager().createNewPactProgram("TestProg");
		} catch (PactValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(pactProgram);

		// check if new tabContainer exists
		assertEquals(tabContainerManager.getTabContainers().size(), 1);
		TabContainer tabContainer = tabContainerManager.getTabContainers().get(pactProgram.getId());
		assertNotNull(tabContainer);

		// add another PactProgram to model (via main presenterMenu)
		PactProgram pactProgram1 = null;
		try {
			pactProgram1 = appInjectorMock.getPactProgramManager().createNewPactProgram("TestProg1");
		} catch (PactValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(pactProgram1);

		// check if new tabContainer exists
		assertEquals(tabContainerManager.getTabContainers().size(), 2);
		tabContainer = tabContainerManager.getTabContainers().get(pactProgram1.getId());
		assertNotNull(tabContainer);

		// now close program again - and check if item is removed
		appInjectorMock.getPactProgramManager().removePactProgram(pactProgram);
		tabContainer = tabContainerManager.getTabContainers().get(pactProgram.getId());
		assertNull(tabContainer);
		assertEquals(tabContainerManager.getTabContainers().size(), 1);

		// now close 2. program - and check if item is removed
		appInjectorMock.getPactProgramManager().removePactProgram(pactProgram1);
		tabContainer = tabContainerManager.getTabContainers().get(pactProgram1.getId());
		assertNull(tabContainer);
		assertEquals(tabContainerManager.getTabContainers().size(), 0);
	}
}
