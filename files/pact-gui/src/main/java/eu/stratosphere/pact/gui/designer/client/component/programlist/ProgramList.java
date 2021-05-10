package eu.stratosphere.pact.gui.designer.client.component.programlist;

import java.util.TreeMap;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.IsWidget;

import eu.stratosphere.pact.gui.designer.client.event.PactProgramAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramAddEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramRemoveEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramSelectEvent;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListItem.ProgramListItemViewInterface;

/**
 * Widget representing the pactProgram Accordion panel on the left
 */
public class ProgramList {
	private TreeMap<Integer, ProgramListItem> programListItems = new TreeMap<Integer, ProgramListItem>();

	/**
	 * only for testing
	 * 
	 * @return
	 */
	public TreeMap<Integer, ProgramListItem> getProgramListItems() {
		return programListItems;
	}

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Interface defines needed methods of all view (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public interface ProgramListViewInterface extends IsWidget {
		void setActiveProgramListItem(ProgramListItem programListItem);

		void addListViewItem(ProgramListItemViewInterface programListItemView);
	}

	/**
	 * reference to view used in this presenter
	 */
	private ProgramListViewInterface programListView;

	public ProgramListViewInterface getView() {
		return programListView;
	}

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 */
	public ProgramList(AppInjector appInjector) {
		super();
		this.appInjector = appInjector;
		this.programListView = appInjector.getProgramListView();

		Log.debug(this + ": Constructor");

		bind();
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	private void bind() {
		Log.debug(this + ": bind");
		/**
		 * when pactProgram was removed from model - remove widget from
		 * accordion
		 */
		appInjector.getEventBus().addHandler(PactProgramRemoveEvent.TYPE, new PactProgramRemoveEventHandler() {
			@Override
			public void onRemovePactProgram(PactProgramRemoveEvent event) {
				action_RemoveItemForPactProgram(event.getPactProgram());
			}
		});

		/**
		 * when pactProgram was added to model - add widget to accordion
		 */
		appInjector.getEventBus().addHandler(PactProgramAddEvent.TYPE, new PactProgramAddEventHandler() {
			@Override
			public void onAddPactProgram(PactProgramAddEvent event) {
				action_AddNewItemForPactProgram(event.getPactProgram());
			}
		});

	}

	/**
	 * when program was removed from model, this method updates list of programs
	 * in accordion and the view
	 * 
	 * @param pactProgram
	 */
	private void action_RemoveItemForPactProgram(PactProgram pactProgram) {
		if (pactProgram != null) {
			ProgramListItem guiPactProgramListItem = programListItems.get(pactProgram.getId());

			if (guiPactProgramListItem == null) {
				Log.info("GuiPactProgramList - No accordion item found for pact program.");
			} else {
				// remove from DOM
				guiPactProgramListItem.remove();

				// remove from presenter
				programListItems.remove(pactProgram.getId());

				PactProgram newPactProgramToSelect = null;
				if (programListItems.size() > 0) {
					int newActiveId = programListItems.firstKey();
					programListView.setActiveProgramListItem(programListItems.get(newActiveId));
					newPactProgramToSelect = appInjector.getPactProgramManager().getPactProgramById(newActiveId);
				} else {
					Log.debug("No child pact program items");
					programListView.setActiveProgramListItem(null);
				}

				appInjector.getEventBus().fireEvent(new PactProgramSelectEvent(newPactProgramToSelect));
			}
		}
	}

	/**
	 * when program was added to model, this method updates list of programs
	 * in accordion and the view
	 * 
	 * @param pactProgram
	 */
	private ProgramListItem action_AddNewItemForPactProgram(PactProgram newPactProgram) {
		Log.debug("onAddPactProgram");

		ProgramListItem programListItem = null;
		if (newPactProgram != null) {
			programListItem = programListItems.get(newPactProgram.getId());
			if (programListItem == null) {
				programListItem = new ProgramListItem(appInjector, newPactProgram);
				programListItems.put(newPactProgram.getId(), programListItem);
				programListView.addListViewItem(programListItem.getView());
				programListView.setActiveProgramListItem(programListItem);
				appInjector.getEventBus().fireEvent(new PactProgramSelectEvent(newPactProgram));
			} else {
				Log.warn("Try to add PactProgram to ProgramList, but PactProgram-id already exists");
			}
		}

		return programListItem;
	}

	public String toString() {
		return "ProgramList";
	}
}