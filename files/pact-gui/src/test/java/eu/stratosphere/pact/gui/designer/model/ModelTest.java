package eu.stratosphere.pact.gui.designer.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;
import eu.stratosphere.pact.gui.designer.shared.model.Connection;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;

public class ModelTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	@Test
	public void test() {
		// create pact program manager
		PactProgramManager pactProgramManager = appInjectorMock.getPactProgramManager();
		assertNotNull(pactProgramManager);

		// try to create new pact program
		PactProgram pactProgram = null;
		try {
			pactProgram = pactProgramManager.createNewPactProgram("__NAME WONT WORK");
		} catch (PactValidationException e) {

		}
		assertNull(pactProgram);
		assertEquals(0, pactProgramManager.getPactPrograms().size());

		// now create correct program
		try {
			pactProgram = pactProgramManager.createNewPactProgram("Test1");
		} catch (PactValidationException e) {

		}
		assertNotNull(pactProgram);
		assertEquals(1, pactProgramManager.getPactPrograms().size());
		assertEquals(pactProgram, pactProgramManager.getPactProgramById(pactProgram.getId()));

		// check if first pact would have id 1
		assertEquals(1, pactProgram.getNextObjectId());

		// create pacts
		Pact pact1 = pactProgram.createNewPact(PactType.SOURCE);
		assertNotNull(pact1);
		Pact pact2 = pactProgram.createNewPact(PactType.SINK);
		assertNotNull(pact2);

		// check if first pact has id 1
		assertEquals(1, pact1.getId());
		assertEquals(2, pact2.getId());

		// test retreiving of pacts by id
		Pact testPact;
		testPact = pactProgram.getPactById(pact1.getId());
		assertEquals(testPact, pact1);

		// check if next Connection would have id 3
		int nextId = pactProgram.getConnections().size() + pactProgram.getPacts().size() + 1;
		assertEquals(nextId, pactProgram.getNextObjectId());

		// create connection
		Connection connection1 = pactProgram.createNewConnection();
		assertNotNull(connection1);
		assertEquals(1, pactProgram.getConnections().size());

		// test retreiving of pacts by id
		assertEquals(connection1, pactProgram.getConnectionById(connection1.getId()));

		// check if first Connection has id 3
		assertEquals(nextId, connection1.getId());

		// connect to pacts
		connection1.setFromPact(pact1);
		connection1.setToPact(pact2);

		// check if everthing is connected
		assertEquals(pact1, connection1.getFromPact());
		assertEquals(pact2, connection1.getToPact());

		assertEquals(pact1.getOutputConntections().size(), 1);
		assertEquals(connection1, pact1.getOutputConntections().get(0));
		assertEquals(pact2.getInputConntections().size(), 1);
		assertEquals(connection1, pact2.getInputConntections().get(0));

		// test and change type of pact1
		assertEquals(PactType.SINK, pact2.getType());
		assertEquals(1, pact2.getType().getNumberOfInputs());
		pact2.setType(PactType.PACT_MATCH);
		assertEquals(PactType.PACT_MATCH, pact2.getType());
		assertEquals(2, pact2.getType().getNumberOfInputs());

		// add new connection (and connect to 2. input of match pact)
		Connection connection2 = pactProgram.createNewConnection();
		connection2.setToPact(pact2);
		assertEquals(connection2.getToPact(), pact2);
		connection2.setToChannelNumber(1);
		assertEquals(connection2.getToChannelNumber(), 1);
		assertEquals(pact2.getInputConntections().size(), 2);

		// change pact type again (to one input channel) - check if 2. inout
		// connection is removed
		pact2.setType(PactType.PACT_REDUCE);
		assertEquals(pact2.getInputConntections().size(), 1);
		assertNull(connection2.getToPact());

		pact2.setType(PactType.SOURCE);
		assertEquals(pact2.getInputConntections().size(), 0);
		assertNull(connection1.getToPact());

		// check if sinks and sources ar numbered correctly
		pact1.setType(PactType.SOURCE);
		pact2.setType(PactType.SOURCE);
		assertEquals(pact1.getSourceOrSinkNumber(), 0);
		assertEquals(pact2.getSourceOrSinkNumber(), 1);

		pact1.setType(PactType.SINK);
		assertEquals(pact1.getSourceOrSinkNumber(), 0);
		assertEquals(pact2.getSourceOrSinkNumber(), 0);

		pact2.setType(PactType.SINK);
		assertEquals(pact1.getSourceOrSinkNumber(), 0);
		assertEquals(pact2.getSourceOrSinkNumber(), 1);

		
		// now delete pact and see if connection behaves correctly
		assertEquals(pact1.getOutputConntections().size(), 1);
		assertEquals(connection1.getFromPact(), pact1);
		assertEquals(pactProgram.getPacts().size(), 2);
		assertEquals(pactProgram.getPactById(pact1.getId()), pact1);
		pact1.remove();
		assertEquals(pactProgram.getPacts().size(), 1);
		assertNull(connection1.getFromPact());
		assertNull(pactProgram.getPactById(pact1.getId()));
		
		
		// now delete connection
		assertEquals(pactProgram.getConnections().size(), 2);
		connection1.remove();
		assertEquals(pactProgram.getConnections().size(), 1);
		connection2.remove();
		assertEquals(pactProgram.getConnections().size(), 0);
	}
}
