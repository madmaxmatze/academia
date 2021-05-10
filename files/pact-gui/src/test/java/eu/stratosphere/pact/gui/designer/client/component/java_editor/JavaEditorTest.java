package eu.stratosphere.pact.gui.designer.client.component.java_editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;

public class JavaEditorTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	@Before
	public void setUp() {
	}

	@Test
	public void test() {
		PactProgram pactProgram = appInjectorMock.getPactProgramManager().addTestPactProgram();
		assertEquals(appInjectorMock.getPactProgramManager().getPactPrograms().size(), 1);

		Pact pact = pactProgram.createNewPact(PactType.PACT_MAP);
		Pact pact1 = pactProgram.createNewPact(PactType.SINK);

		// create ProgramList
		JavaCodeEditor javaCodeEditor = new JavaCodeEditor(appInjectorMock, pact);
		assertNotNull(javaCodeEditor);

		// check some value changes
		javaCodeEditor.action_changeDegreeOfParallelism(-100);
		assertEquals(pact.getDegreeOfParallelism(), 1);

		javaCodeEditor.action_changeDegreeOfParallelism(5);
		assertEquals(pact.getDegreeOfParallelism(), 5);

		assertNotNull(javaCodeEditor.action_validateNewPactName("sdf sd&%df"));
		assertNotNull(javaCodeEditor.action_validateNewPactName(pact1.getName()));
		assertNull(javaCodeEditor.action_validateNewPactName(pact.getName()));
		assertNull(javaCodeEditor.action_validateNewPactName("ValidName"));
		String newName = "MyNewName";
		javaCodeEditor.action_changePactNameWithoutValidation(newName);
		assertEquals(pact.getName(), newName);

		javaCodeEditor.action_changePactType(null);
		assertNotNull(pact.getType());
		javaCodeEditor.action_changePactType(PactType.PACT_REDUCE);
		assertEquals(pact.getType(), PactType.PACT_REDUCE);
	}
}
