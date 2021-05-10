package eu.stratosphere.pact.gui.designer.client.presenter;

import org.junit.Before;
import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;

public class LoginPresenterTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	@Before
	public void setUp() {
	}

	@SuppressWarnings("unused")
	@Test
	public void test() {
		// not much to test here
		LoginPresenter loginPresenter = new LoginPresenter(appInjectorMock, appInjectorMock.getLoginView());
	}
}
