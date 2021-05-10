package eu.stratosphere.pact.gui.designer.client.component.programlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;
import eu.stratosphere.pact.gui.designer.client.presenter.MainPresenter;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;

public class ProgramListTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	@Before
	public void setUp() {
	}

	@Test
	public void test() {
		// create MainPresenter (only for easy program creation and deletion)
		MainPresenter mainPresenter = new MainPresenter(appInjectorMock, appInjectorMock.getMainView());
		assertNotNull(mainPresenter);
		
		// create ProgramList
		ProgramList programList = new ProgramList(appInjectorMock);
		assertNotNull(programList);
		
		// test - no programs existing
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 0);
		assertEquals(programList.getProgramListItems().size(), 0);

		// add PactProgram to model (via main presenterMenu)
		// then check if item was added to ProgramList
		PactProgram pactProgram = null;
		try {
			pactProgram = mainPresenter.action_AddPactProgram("TestProg");
		} catch (PactValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(pactProgram);

		// test if added to programList with the right id
		assertEquals(1, programList.getProgramListItems().size());	
		ProgramListItem programListItem = programList.getProgramListItems().get(pactProgram.getId());
		assertNotNull(programListItem);
		assertEquals(pactProgram.getId(), programList.getProgramListItems().firstKey().intValue());
		
		// add another PactProgram to model (via main presenterMenu)
		PactProgram pactProgram1 = null;
		try {
			pactProgram1 = mainPresenter.action_AddPactProgram("TestProg1");
		} catch (PactValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(pactProgram1);

		// test if added to programList with the right id
		assertEquals(2, programList.getProgramListItems().size());	
		programListItem = programList.getProgramListItems().get(pactProgram1.getId());
		assertNotNull(programListItem);
		
		// now close first program again - and check if item is removed and sec, program now the first one
		mainPresenter.action_ClosePactProgram(pactProgram);
		programListItem = programList.getProgramListItems().get(pactProgram.getId());
		assertNull(programListItem);
		assertEquals(pactProgram1.getId(), programList.getProgramListItems().firstKey().intValue());
	
		// remove last remaining pact program - check if empty again
		mainPresenter.action_ClosePactProgram(pactProgram1);
		programListItem = programList.getProgramListItems().get(pactProgram1.getId());
		assertNull(programListItem);
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 0);
		assertEquals(programList.getProgramListItems().size(), 0);
	}
}
