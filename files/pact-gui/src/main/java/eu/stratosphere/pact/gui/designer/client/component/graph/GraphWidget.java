package eu.stratosphere.pact.gui.designer.client.component.graph;

import java.util.ArrayList;
import java.util.HashMap;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.widget.core.client.info.Info;

import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidget.GraphWidgetViewInterface.ClickEventType;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.DraggableGuiObject;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.DroppableGuiObjectController;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.GuiConnection;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.GuiPact;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionAddEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionPropertyChangeEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.GlobalKeyPressEvent;
import eu.stratosphere.pact.gui.designer.client.event.GlobalKeyPressEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactAddEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.UserFunctionOpenEvent;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.Connection;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;

/**
 * Widget to design DAG for PactProgram - usually put into a tab panel
 */
public class GraphWidget {
	private GraphWidget self = this;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Model behind this widget
	 */
	private PactProgram pactProgram;

	public PactProgram getPactProgram() {
		return pactProgram;
	}

	/**
	 * reference to passed view
	 */
	private GraphWidgetViewInterface graphWidgetView = null;

	public GraphWidgetViewInterface getView() {
		return graphWidgetView;
	}

	/**
	 * Interface defines needed methods of all views (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public interface GraphWidgetViewInterface extends IsWidget {
		/**
		 * Types of Buttons a view needs to provide for the main presenter
		 */
		static public enum ClickEventType {
			BUTTON_NEW_PACT, BUTTON_NEW_SINK, BUTTON_NEW_SOURCE, BUTTON_NEW_JAVA, BUTTON_NEW_CONNECTION,

			BUTTON_REMOVE, BUTTON_EDIT
		}

		AbsolutePanel getGraphContainer();

		void prepareWidget(GraphWidget graphWidget);

		void addClickEvent(ClickEventType clickEventTypeType, ClickHandler clickHandler);

		void handleClickOnConnectionOrBackground(int x, int y);

		GuiConnection addOrGetGuiConnectionForConnection(Connection connection);

		void addGuiPactForPact(Pact pact);

		IsWidget getFocusWidget();

		void refreshGuiConnections(Connection connection);

		void registerDropController(DroppableGuiObjectController guiObjectInputDropController);

		void makeDraggable(GuiPact guiPact);

		void setFocusWidget(DraggableGuiObject draggableGuiObject);

		void refreshGuiConnectionsOfConnections(ArrayList<Connection> allConnections);

		void unregisterDropController(DroppableGuiObjectController guiPactDropAreaController);

		HashMap<Integer, GuiPact> getGuiPacts();

		HashMap<Integer, GuiConnection> getGuiConnections();

		void refreshGuiConnections(ArrayList<GuiConnection> currentGuiConnectionsDuringDrag);

		void resizeContainer();
	};

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 * @param pactProgram
	 */
	public GraphWidget(AppInjector appInjector, PactProgram pactProgram) {
		this.appInjector = appInjector;
		this.pactProgram = pactProgram;

		Log.debug(this + ": Constructor");

		graphWidgetView = appInjector.getGraphWidgetView();
		graphWidgetView.prepareWidget(this);

		bind();
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	public void bind() {
		// attach click handler to top button bar
		graphWidgetView.addClickEvent(ClickEventType.BUTTON_NEW_PACT, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": New Pact Button Click - add Map Pact");
				action_AddMapPact();
			}
		});

		graphWidgetView.addClickEvent(ClickEventType.BUTTON_NEW_SINK, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": New Sink Button Click");
				action_AddSink();
			}
		});

		graphWidgetView.addClickEvent(ClickEventType.BUTTON_NEW_SOURCE, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": New Source Button Click");
				action_AddSource();
			}
		});

		graphWidgetView.addClickEvent(ClickEventType.BUTTON_NEW_CONNECTION, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": New Connection Button Click");
				action_AddConnection();
			}
		});

		graphWidgetView.addClickEvent(ClickEventType.BUTTON_NEW_JAVA, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": New Java Button Click");
				action_AddJavaPact();
			}
		});

		graphWidgetView.addClickEvent(ClickEventType.BUTTON_REMOVE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				IsWidget focusedWidget = graphWidgetView.getFocusWidget();
				if (focusedWidget != null) {
					Log.debug(this + ": New RemoveFocuesWidget Button Click");
					((DraggableGuiObject) focusedWidget).removeWithQuestion();
				}
			}
		});

		graphWidgetView.addClickEvent(ClickEventType.BUTTON_EDIT, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				IsWidget focusedWidget = graphWidgetView.getFocusWidget();
				if (focusedWidget != null) {
					Log.debug(self + ": New EditFocuesWidget Button Click");
					if (focusedWidget instanceof GuiPact) {
						Pact pactToFocus = ((GuiPact) focusedWidget).getPact();
						appInjector.getEventBus().fireEvent(new UserFunctionOpenEvent(pactToFocus));
					} else {
						Info.display("Edit Connection", "Connection editing not implemented so far");
					}
				}
			}
		});

		// if model was changed - refresh view
		appInjector.getEventBus().addHandler(PactAddEvent.TYPE, new PactAddEventHandler() {
			@Override
			public void onPactAdd(PactAddEvent event) {
				if (event.getPact().getPactProgram() == pactProgram) {
					Log.debug(self + ": " + event + " catched");
					graphWidgetView.addGuiPactForPact(event.getPact());
				}
			}
		});

		// if model was changed - refresh view
		appInjector.getEventBus().addHandler(ConnectionAddEvent.TYPE, new ConnectionAddEventHandler() {
			@Override
			public void onConnectionAdd(ConnectionAddEvent event) {
				if (event.getConnection().getPactProgram() == pactProgram) {
					Log.debug(self + ": " + event + " catched");
					graphWidgetView.addOrGetGuiConnectionForConnection(event.getConnection());
				}
			}
		});

		// if model was changed - refresh view
		appInjector.getEventBus().addHandler(ConnectionPropertyChangeEvent.TYPE, new ConnectionPropertyChangeEventHandler() {
			@Override
			public void onConnectionPropertyChange(ConnectionPropertyChangeEvent event) {
				if (event.getConnection().getPactProgram() == pactProgram) {
					Log.debug(self + ": ConnectionPropertyChange Event catched");
					graphWidgetView.refreshGuiConnections(event.getConnection());
				}
			}
		});
		
		// if model was changed - refresh view
		appInjector.getEventBus().addHandler(GlobalKeyPressEvent.TYPE, new GlobalKeyPressEventHandler() {
			@Override
			public void onGlobalKeyPress(GlobalKeyPressEvent event) {
				/*
				 * if (self.isVisible()) { // if current pactProgramContainer is
				 * // active if (KeyCodes.KEY_DELETE ==
				 * event.getNativeKeyEvent().getNativeKeyCode()) {
				 * Info.display("Key Press", "KeyCode: " +
				 * event.getNativeKeyEvent().getNativeKeyCode());
				 * 
				 * DraggableGuiObject guiObject = (DraggableGuiObject)
				 * graphWidget.getFocusWidget(); if (guiObject != null) {
				 * guiObject.removeWithQuestion(); } } }
				 */
			}
		});
	}

	/**
	 *  method wrapper for testing
	 */
	protected Pact action_AddJavaPact() {
		return action_AddPact(PactType.JAVA);
	}

	/**
	 *  method wrapper for testing
	 */
	protected Pact action_AddSource() {
		return action_AddPact(PactType.SOURCE);
	}

	/**
	 *  method wrapper for testing
	 */
	protected Pact action_AddSink() {
		return action_AddPact(PactType.SINK);
	}

	/**
	 *  method wrapper for testing
	 */
	protected Pact action_AddMapPact() {
		return action_AddPact(PactType.PACT_MAP);
	}

	/**
	 * Add pact to model and gui
	 */
	private Pact action_AddPact(PactType pactType) {
		Log.debug(this + ": addPact of type " + pactType);

		Pact newPact = pactProgram.createNewPact(pactType);
		newPact.setX(50, false);
		newPact.setY(50, true);

		return newPact;
	}

	/**
	 * Add connection to model and gui
	 */
	public Connection action_AddConnection() {
		Log.debug(this + ": addConnection");

		Connection newConnection = pactProgram.createNewConnection();
		newConnection.setFromX(40, false);
		newConnection.setFromY(40, false);
		newConnection.setToX(200, false);
		newConnection.setToY(40, true);

		return newConnection;
	}

	public String toString() {
		return "GraphWidget(pactProgram: " + pactProgram.getName() + ")";
	}
}
