package eu.stratosphere.pact.gui.designer.client.view;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import eu.stratosphere.pact.gui.designer.client.presenter.MainPresenter.MainViewInterface;

public class MainView extends Composite implements MainViewInterface {
	/**
	 * UiBinder for MainView
	 */
	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	/**
	 * Interface for UiBinder for MainView
	 */
	interface MainViewUiBinder extends UiBinder<Widget, MainView> {
	}

	/**
	 * Automatically assign attributes for whole layout
	 */
	@UiField
	BorderLayoutContainer layout;

	@UiField
	TextButton menuItem_logout;

	@UiField
	TextButton menuItem_program;

	@UiField
	MenuItem program_new;

	@UiField
	MenuItem program_open;

	@UiField
	MenuItem program_save;

	@UiField
	MenuItem program_compile;

	@UiField
	MenuItem program_close;

	/**
	 * Getter for layout - to fill panels from presenter
	 */
	public BorderLayoutContainer getLayout() {
		return layout;
	}

	public MainView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void addClickEvent(ButtonType buttonType, ClickHandler clickHandler) {
		switch (buttonType) {
		case PROGRAM_NEW:
			program_new.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case PROGRAM_OPEN:
			program_open.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case PROGRAM_SAVE:
			program_save.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case PROGRAM_COMPILE:
			program_compile.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case PROGRAM_CLOSE:
			program_close.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case LOGOUT:
			menuItem_logout.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		default:
			Log.error(this + ": try to add unknown Click Event for " + buttonType);
			break;
		}
	}

	@Override
	public void setMenuItemEnable(ButtonType buttonType, boolean menuItemsShouldBeEnabled) {
		switch (buttonType) {
		case PROGRAM_NEW:
			program_new.setEnabled(menuItemsShouldBeEnabled);
			break;
		case PROGRAM_OPEN:
			program_open.setEnabled(menuItemsShouldBeEnabled);
			break;
		case PROGRAM_SAVE:
			program_save.setEnabled(menuItemsShouldBeEnabled);
			break;
		case PROGRAM_COMPILE:
			program_compile.setEnabled(menuItemsShouldBeEnabled);
			break;
		case PROGRAM_CLOSE:
			program_close.setEnabled(menuItemsShouldBeEnabled);
			break;
		case LOGOUT:
			menuItem_logout.setEnabled(menuItemsShouldBeEnabled);
			break;
		default:
			break;
		}
	}
}