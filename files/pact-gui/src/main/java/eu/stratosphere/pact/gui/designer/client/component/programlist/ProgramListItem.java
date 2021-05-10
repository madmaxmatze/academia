package eu.stratosphere.pact.gui.designer.client.component.programlist;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.widget.core.client.event.ExpandEvent;
import com.sencha.gxt.widget.core.client.event.ExpandEvent.ExpandHandler;

import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListItem.ProgramListItemViewInterface.ButtonType;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramSelectEvent;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.JarFile;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;

/**
 * Single Item in PactProgramList Widget (Accordion on the left)
 */
public class ProgramListItem {
	/**
	 * Interface defines needed methods of all views (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public static interface ProgramListItemViewInterface extends IsWidget {
		/**
		 * Types of Buttons a view needs to provide for the main presenter
		 */
		static public enum ButtonType {
			JAR_ADD, JAR_REMOVE;
		}

		void remove();

		void bindExpandHandler(ExpandHandler expandHandler);

		void addClickEvent(ButtonType buttonType, ClickHandler clickHandler);

		void showJarUploadForm(JarUploadHandler jarUploadHandler);

		JarFile getSelectedJarFile();

		void prepareWidget(ProgramListItem programListItem);

		void expand();
	}

	/**
	 * Connected pactProgram
	 */
	private PactProgram pactProgram;

	public PactProgram getPactProgram() {
		return pactProgram;
	}

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * reference to passed view
	 */
	private ProgramListItemViewInterface programListItemView;

	public ProgramListItemViewInterface getView() {
		return programListItemView;
	}

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 * @param pactProgram
	 */
	public ProgramListItem(AppInjector appInjector, PactProgram pactProgram) {
		Log.info(this + ": Constructor");

		this.pactProgram = pactProgram;
		this.appInjector = appInjector;

		programListItemView = appInjector.getProgramListItemView();
		programListItemView.prepareWidget(this);

		bind();
		programListItemView.expand();
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	private void bind() {
		Log.info(this + ": bind");

		/**
		 * when a new pactProgram is selected in accordion on the left
		 */
		programListItemView.bindExpandHandler(new ExpandHandler() {
			@Override
			public void onExpand(ExpandEvent event) {
				appInjector.getEventBus().fireEvent(new PactProgramSelectEvent(pactProgram));
			}
		});

		programListItemView.addClickEvent(ButtonType.JAR_ADD, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				programListItemView.showJarUploadForm(new JarUploadHandler(appInjector, pactProgram));
			}
		});

		// remove jar button
		programListItemView.addClickEvent(ButtonType.JAR_REMOVE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				JarFile jarFile = programListItemView.getSelectedJarFile();
				if (jarFile != null) {
					pactProgram.removeJarFile(jarFile);
				}
			}
		});
	}

	/**
	 * remove item from list
	 */
	public void remove() {
		programListItemView.remove();
	}

	public String toString() {
		return "ProgramListItem for " + pactProgram;
	}
}