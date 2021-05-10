package eu.stratosphere.pact.gui.designer.client.presenter;

import java.util.Date;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import eu.stratosphere.pact.gui.designer.client.event.ComponentPingEvent;
import eu.stratosphere.pact.gui.designer.client.event.ComponentPingEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.presenter.LoginPresenter.LoginViewInterface.ClickEventType;

/**
 * Presenter (within the MVP pattern for the first login page)
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class LoginPresenter implements Presenter {
	/**
	 * View attached to this presenter (MVP)
	 */
	private final LoginViewInterface loginView;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Interface defines needed methods of all view (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public interface LoginViewInterface {
		/**
		 * Types of Buttons a view needs to provide for the main presenter
		 */
		static public enum ClickEventType {
			OK_BUTTON;
		}

		void setLoginLabelText(String text);

		String getEnteredUsername();

		String getEnteredPassword();

		void addClickEventHandler(ClickEventType clickEventType, ClickHandler clickHandler);

		void addInputBoxesKeyDownEventHandler(KeyDownHandler keyDownHandler);
	}

	/**
	 * Presenter is constructed with dependency injection object and attached
	 * view
	 * 
	 * @param appInjector
	 * @param view
	 */
	@Inject
	public LoginPresenter(AppInjector appInjector, LoginViewInterface loginView) {
		this.appInjector = appInjector;
		this.loginView = loginView;

		bind();
	}

	/**
	 * Initialization is done in constructor. This seperate method actually
	 * attaches the presenter to the viewport
	 */
	public void go(final HasWidgets container) {
		container.clear();
		container.add((Widget) loginView);
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	public void bind() {
		Log.debug("bind");

		// Just for developing purpose to check if presenter is garbage
		// collected after removing from view port
		// for more information on this - see AppController:
		// customEventBus.removeHandlers();
		appInjector.getEventBus().addHandler(ComponentPingEvent.TYPE, new ComponentPingEventHandler() {
			@Override
			public void onPing(ComponentPingEvent event) {
				Log.debug("Ping: LoginPresenter");
			}
		});

		loginView.addClickEventHandler(ClickEventType.OK_BUTTON, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				action_DoLoginWithFormValues();
			}
		});
		
		loginView.addInputBoxesKeyDownEventHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					action_DoLoginWithFormValues();
				} else {
					loginView.setLoginLabelText("");
				}
			}
		});
	}

	/**
	 * Wrapper for better testability
	 */
	private void action_DoLoginWithFormValues() {
		action_DoLogin(loginView.getEnteredUsername(), loginView.getEnteredPassword());
	}

	/**
	 * Method to actually perform a login using the passed credentials
	 */
	private void action_DoLogin(String user, String password) {
		Log.debug("doLogin");

		/*
		 * Ajax request to login - when successful redirect to #main-page
		 */
		appInjector.getAjaxService().login(user, password,
		// callback for ajax function
				new AsyncCallback<String>() {
					public void onSuccess(String sessionID) {
						Log.debug("Login onSuccess-Callback-Handler. SessionID:" + sessionID);

						// when new sessionID exists, serverlogin was successful
						if (sessionID != null) {
							Log.debug("Login success. SessionID:" + sessionID);

							// save session id for 2 weeks
							final long DURATION = 1000 * 60 * 60 * 24 * 14;
							Date expires = new Date(System.currentTimeMillis() + DURATION);
							Cookies.setCookie("sid", sessionID, expires, null, "/", false);

							// redirect to #main page
							Log.debug("New History Item: main");
							History.newItem("main");
						} else {
							// login not successful: show error message
							Log.debug("Login failed.");
							loginView.setLoginLabelText("User/PW incorrect. Try a user starting with \"a\"");
						}
					}

					// on failure, show error message
					public void onFailure(Throwable caught) {
						loginView.setLoginLabelText("Error occured while logging in: " + caught.toString());
						Log.error("Failed Login - RPC: " + caught.getMessage());
					}
				});
	}
}
