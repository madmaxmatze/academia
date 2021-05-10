package eu.stratosphere.pact.gui.designer.client.component.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;

public class GraphWidgetTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	@Before
	public void setUp() {
	}

	@Test
	public void test() {
		// create MainPresenter (only for easy program creation and deletion)		
		PactProgramManager pactProgramManager = appInjectorMock.getPactProgramManager();
		
		PactProgram pactProgram = null;
		try {
			pactProgram = pactProgramManager.createNewPactProgram("TestProg");
		} catch (PactValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(pactProgram);
		
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 1);
		
		// create GraphWidget
		GraphWidget graphWidget = new GraphWidget(appInjectorMock, pactProgram);
		
		// pactProgram set correctly?
		assertEquals(graphWidget.getPactProgram(), pactProgram);
		
		// create Pact
		int newPactId = pactProgram.getNextObjectId();
		Pact mapPact = graphWidget.action_AddMapPact();
		assertEquals(pactProgram.getPacts().size(), 1);
		assertEquals(pactProgram.getPactById(mapPact.getId()), mapPact);
		assertEquals(mapPact.getId(), newPactId);
		assertEquals(PactType.PACT_MAP, mapPact.getType());
		
		Pact sourcePact = graphWidget.action_AddSource();
		assertEquals(PactType.SOURCE, sourcePact.getType());
		
		Pact sinkPact = graphWidget.action_AddSink();
		assertEquals(PactType.SINK, sinkPact.getType());
		
		Pact javaPact = graphWidget.action_AddJavaPact();
		assertEquals(PactType.JAVA, javaPact.getType());
		
		assertEquals(pactProgram.getPacts().size(), 4);
	}
}
