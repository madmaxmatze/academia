package eu.stratosphere.pact.gui.designer.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

import eu.stratosphere.pact.gui.designer.client.event.ComponentPingEvent;
import eu.stratosphere.pact.gui.designer.client.event.ExceptionEvent;
import eu.stratosphere.pact.gui.designer.client.event.ExceptionEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.CustomEventBus;
import eu.stratosphere.pact.gui.designer.client.i18n.StringConstants;
import eu.stratosphere.pact.gui.designer.client.presenter.LoginPresenter;
import eu.stratosphere.pact.gui.designer.client.presenter.MainPresenter;
import eu.stratosphere.pact.gui.designer.client.presenter.Presenter;

/**
 * The AppController controls which page is presented to the user (#login,
 * #main) and invokes the appropriate presenter.
 */
public class AppController implements Presenter, ValueChangeHandler<String> {
	/**
	 * Create an AppController Objects
	 */
	public AppController(AppInjector appInjector) {
		this.appInjector = appInjector;
		bind();
	}

	/**
	 * Passed Viewport - actually the <body> element of the page
	 */
	private HasWidgets container;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;
	
	/**
	 * Bind all event handlers
	 */
	private void bind() {
		/*
		 * The current app controller itself should handle
		 * historyValueChangeEvents --> see method onValueChange
		 */
		History.addValueChangeHandler(this);

		/**
		 * Handle app wide thrown exceptions - mainly inform the user
		 */
		appInjector.getEventBus().addHandler(ExceptionEvent.TYPE, new ExceptionEventHandler() {
			@Override
			public void onException(ExceptionEvent exceptionEvent) {
				StringConstants myConstants = GWT.create(StringConstants.class);
				String output = myConstants.exceptionText() + exceptionEvent.getException().getMessage();
				Log.error(output);
				Window.alert(output);
			}
		});
	}

	/**
	 * Initialization is done in constructor. This seperate method actually
	 * fires the page rendering based on the History (or in other words url)
	 */
	public void go(final HasWidgets container) {
		this.container = container;

		if ("".equals(History.getToken())) {
			History.newItem("main");
		} else {
			History.fireCurrentHistoryState();
		}
	}

	/**
	 * Listens to value changes of the History object and then reacts with
	 * creating the correct presenter
	 */
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		final String token = event.getValue();
		Log.info("onValueChange:" + token);

		if (token != null) {
			Log.info("Check if allowed: " + token);
			String sessionID = Cookies.getCookie("sid");

			// if no session exists
			if (sessionID == null) {
				// and we are at #login
				if (token.equals("login")) {
					// just show login screen
					openPresenter("login");
				} else {
					// forward to login
					History.newItem("login");
				}
			} else {

				/*
				 * If the session cookie exists, do a server roundtrip to check
				 * if the session is valid
				 */
				try {
					Log.info("Ask if logged in");
					appInjector.getAjaxService().isLoggedIn(sessionID, new AsyncCallback<Boolean>() {
						public void onSuccess(Boolean result) {
							Boolean isLoggedIn = (Boolean) result;
							// do some UI stuff to show success
							if (isLoggedIn) {
								Log.info("Logged in. So forward to:" + token);
								if ("login".equals(token)) {
									History.newItem("main");
								} else {
									openPresenter(token);
								}
							} else {
								Log.info("Not logged in - go to login");
								if ("login".equals(token)) {
									openPresenter("login");
								} else {
									History.newItem("login");
								}
							}
						}

						public void onFailure(Throwable caught) {
							// do some UI stuff to show failure
							Log.error("Failed isLoggedIn-RPC: " + caught.getMessage());
						}
					});
				} catch (Exception ex) {
					// Exception in RPC invocation
					appInjector.getEventBus().fireEvent(new ExceptionEvent(ex));
				}
			}
		}
	}

	/**
	 * Actually initializing a present according to passed presenterId
	 * 
	 * @param presenterId
	 */
	private void openPresenter(String presenterId) {
		/*
		 * presenter stay in memory as ComponentPingEvent shows. This is because
		 * the event bus hold pointer to eventhandlers within presenter.
		 * Solution is to remove all current registered events
		 * 
		 * More about this:
		 * http://www.draconianoverlord.com/2010/11/23/gwt-handlers.html
		 */
		CustomEventBus customEventBus = ((CustomEventBus) appInjector.getEventBus());
		customEventBus.removeHandlers();

		Presenter newPresenter = null;
		if ("login".equals(presenterId)) {
			newPresenter = new LoginPresenter(appInjector, appInjector.getLoginView());

		} else if ("main".equals(presenterId)) {
			newPresenter = new MainPresenter(appInjector, appInjector.getMainView());
		}

		if (newPresenter != null) {
			newPresenter.go(container);
		}

		// to check that only one component exists
		appInjector.getEventBus().fireEvent(new ComponentPingEvent());
	}
}
