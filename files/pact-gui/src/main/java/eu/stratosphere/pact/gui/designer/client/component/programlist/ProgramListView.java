package eu.stratosphere.pact.gui.designer.client.component.programlist;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;

import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListItem.ProgramListItemViewInterface;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;

/**
 * Widget representing the pactProgram Accordion panel on the left
 */
public class ProgramListView extends AccordionLayoutContainer implements ProgramList.ProgramListViewInterface {
	/**
	 * Dependency Injection object
	 */
	// private AppInjector appInjector;

	/**
	 * Interface defines needed methods of all view (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public static interface ProgramListViewInterface {

	}

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 */
	@Inject
	public ProgramListView(AppInjector appInjector) {
		super();
		// this.appInjector = appInjector;
		Log.debug(this + ": Constructor");

		setFill(true);
		setBorders(false);
	}

	public String toString() {
		return "ProgramListView";
	}

	/**
	 * when the active pact program is removed. The presenter needs to define a
	 * new active program
	 */
	@Override
	public void setActiveProgramListItem(ProgramListItem programListItem) {
		this.setWidget(programListItem.getView());
	}

	@Override
	public void addListViewItem(ProgramListItemViewInterface programListItemView) {
		try {
			this.add((Widget) programListItemView);
		} catch (Throwable e) { // needed to also catch asserterror
			Log.warn("Create Accordion Item - " + e.getClass().getName() + " catched: " + e.getMessage());
		}
		this.setWidget(programListItemView);

		// programListItemView.dummyExpand();

		this.syncSize();
		this.clearSizeCache();
		this.forceLayout();
	}
}