package eu.stratosphere.pact.gui.designer.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;
import eu.stratosphere.pact.gui.designer.shared.model.Connection;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;

public class ModelValidationTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	private static Logger Log = Logger.getLogger("");

	@Test
	public void test() {
		// create pact program manager
		PactProgramManager pactProgramManager = appInjectorMock.getPactProgramManager();
		assertNotNull(pactProgramManager);

		// try to create new pact program
		PactProgram pactProgram = null;
		try {
			pactProgram = pactProgramManager.createNewPactProgram("TestProg");
		} catch (PactValidationException e) {

		}
		assertNotNull(pactProgram);
		assertEquals(1, pactProgramManager.getPactPrograms().size());

		// create a valid wordcount example with 4 pact and 3 connections
		Pact source = pactProgram.createNewPact(PactType.SOURCE);
		Pact map = pactProgram.createNewPact(PactType.PACT_MAP);
		Pact reduce = pactProgram.createNewPact(PactType.PACT_REDUCE);
		Pact sink = pactProgram.createNewPact(PactType.SINK);

		Connection sourceToMapConnection = pactProgram.createNewConnection();
		Connection mapToReduceConnection = pactProgram.createNewConnection();
		Connection reduceToSinkConnection = pactProgram.createNewConnection();

		sourceToMapConnection.setFromPact(source);
		sourceToMapConnection.setToPact(map);

		mapToReduceConnection.setFromPact(map);
		mapToReduceConnection.setToPact(reduce);

		reduceToSinkConnection.setFromPact(reduce);
		reduceToSinkConnection.setToPact(sink);

		source.setJavaCode("public class " + source.getName() + " extends DelimitedInputFormat {}");
		map.setJavaCode("public class " + map.getName() + " extends MapStub {}");
		reduce.setJavaCode("public class " + reduce.getName() + " extends ReduceStub {}");
		sink.setJavaCode("public class " + sink.getName() + " extends FileOutputFormat {}");

		ArrayList<Error> validationErrors = null;
		validationErrors = pactProgram.getValidationErrors();
		if (validationErrors.size() > 0) {
			// otherwise output errors to log
			for (Error error : pactProgram.getValidationErrors()) {
				Log.severe(error.getMessage());
			}
		}
		assertTrue(validationErrors.size() == 0);


		// now change little things and force validation errors
		// wrong class Name
		source.setJavaCode("public class DUMMY" + source.getName() + " extends DelimitedInputFormat {}");
		validationErrors = pactProgram.getValidationErrors();
		assertTrue(validationErrors.size() != 0);

		// correct wrong class name
		source.setName("DUMMY" + source.getName());
		validationErrors = pactProgram.getValidationErrors();
		assertTrue(validationErrors.size() == 0);

		// unattched connection
		sourceToMapConnection.setFromPact(null);
		validationErrors = pactProgram.getValidationErrors();
		assertTrue(validationErrors.size() != 0);

		// restore connection
		sourceToMapConnection.setFromPact(source);
		validationErrors = pactProgram.getValidationErrors();
		assertTrue(validationErrors.size() == 0);

		// wrong (not existing) channel number
		sourceToMapConnection.setToChannelNumber(5);
		validationErrors = pactProgram.getValidationErrors();
		assertTrue(validationErrors.size() != 0);

		// correct channel number
		sourceToMapConnection.setToChannelNumber(0);
		validationErrors = pactProgram.getValidationErrors();
		assertTrue(validationErrors.size() == 0);
	}
}
