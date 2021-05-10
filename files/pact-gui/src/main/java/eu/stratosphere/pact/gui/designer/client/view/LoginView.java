package eu.stratosphere.pact.gui.designer.client.view;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;

import eu.stratosphere.pact.gui.designer.client.presenter.LoginPresenter;

/*
 * View Class in MVP for Login Presenter
 * 
 * How to use UiBinder: http://www.giantflyingsaucer.com/blog/?p=2302
 * MVP + uiBinder with annotations: http://stackoverflow.com/questions/6313592/uibinder-and-mvp-in-gwt
 */
public class LoginView extends Composite implements LoginPresenter.LoginViewInterface {
	/**
	 * UiBinder for Login View
	 */
	private static LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);

	/**
	 * Interface for UiBinder for Login View
	 */
	interface LoginViewUiBinder extends UiBinder<Component, LoginView> {
	}

	/**
	 * Automatically assign attributes for username Textbox Widget
	 */
	@UiField
	TextBox usernameInput;

	/**
	 * Automatically assign attributes for password Textbox Widget
	 */
	@UiField
	PasswordTextBox passwordInput;

	/**
	 * Automatically assign attributes for login button Widget
	 */
	@UiField
	Button login;

	/**
	 * Automatically assign attributes for login textlabel Widget
	 */
	@UiField
	Label loginLabel;

	/**
	 * Automatically assign attributes for whole login-Widget
	 */
	@UiField
	ContentPanel loginPanel;

	/**
	 * Constructor
	 */
	public LoginView() {
		initWidget(uiBinder.createAndBindUi(this));
		usernameInput.setWidth("200px");
		passwordInput.setWidth("200px");
		loginPanel.setShadow(true);
	}

	/**
	 * Getter for entered username
	 */
	@Override
	public String getEnteredUsername() {
		return usernameInput.getText();
	}

	/**
	 * Getter for entered password
	 */
	@Override
	public String getEnteredPassword() {
		return passwordInput.getText();
	}

	@Override
	public void setLoginLabelText(String text) {
		loginLabel.setText(text);
	}

	@Override
	public void addClickEventHandler(ClickEventType clickEventType, ClickHandler clickHandler) {
		switch (clickEventType) {
		case OK_BUTTON:
			login.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		default:
			Log.error(this + ": try to add unknown Click Event for " + clickEventType);
			break;
		}
	}

	@Override
	public void addInputBoxesKeyDownEventHandler(KeyDownHandler keyDownHandler) {
		passwordInput.addKeyDownHandler(keyDownHandler);
		usernameInput.addKeyDownHandler(keyDownHandler);
	}
}
