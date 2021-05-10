package eu.stratosphere.pact.gui.designer.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ServerFunctionsTest {
	
	@Test
	public void testLoginCreditials() {
		// for now the productive Login is also just a dummy which accepts every user who starts with a
		assertTrue(ServerFuntions.isUserPasswordValid("aaa", "pw"));
		assertFalse(ServerFuntions.isUserPasswordValid("bbb", "pw"));
	}
}
