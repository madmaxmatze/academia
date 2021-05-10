package eu.stratosphere.pact.gui.designer.client.presenter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;

public class MainPresenterTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	/**
	 * JUnit Test for the Main Presenter 
	 *
	 * Programlist and TabContainer are added in View to MainMenu
	 *
	 * Presenter Logic mainly consists of main menu
	 */
	@Test
	public void test() {
		// create MainPresenter - pass mock object
		MainPresenter mainPresenter = new MainPresenter(appInjectorMock, appInjectorMock.getMainView());

		// assure that no programs exist in the beginning
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 0);
	
		// create new test program (check if program size is 1 afterwards)
		appInjectorMock.getPactProgramManager().addTestPactProgram();
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 1);

		// create no program with invalid name (no program added)
		// simuating "Enter name for new pact Program"-PopUp
		try {
			mainPresenter.action_AddPactProgram("INCORRECT PACT NAME )?=");
		} catch (PactValidationException e) {

		}
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 1);

		// add new program by name (click on new menu item, with valid entry)
		try {
			mainPresenter.action_AddPactProgram("MyProgram");
		} catch (PactValidationException e) {

		}
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 2);

		// create pact program via pactProgramManager (open/load menu item)
		PactProgram existingPactProgram = null;
		try {
			existingPactProgram = appInjectorMock.getPactProgramManager().createNewPactProgram("MyProgram2");
		} catch (PactValidationException e) {

		}
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 3);

		// add again existing program (Should not work)
		mainPresenter.action_AddExistingPactProgram(existingPactProgram);
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 3);

		// add null pact program by object (Should not work)
		mainPresenter.action_AddExistingPactProgram(null);
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 3);
		
		// add pact program by object (load menu item)
		PactProgram newPactProgram = new PactProgram();
		mainPresenter.action_AddExistingPactProgram(newPactProgram);
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 4);
		
		// remove pact program (close menu item)
		mainPresenter.action_ClosePactProgram(existingPactProgram);
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 3);

		// remove pact program (close menu item)
		mainPresenter.action_ClosePactProgram(existingPactProgram);
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 3);
		
		// remove pact program (close menu item)
		mainPresenter.action_ClosePactProgram(newPactProgram);
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 2);
	}
}
