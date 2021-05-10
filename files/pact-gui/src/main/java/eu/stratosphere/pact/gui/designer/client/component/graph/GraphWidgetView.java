package eu.stratosphere.pact.gui.designer.client.component.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.ButtonGroup;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidget.GraphWidgetViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.DraggableGuiObject;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.DraggableGuiObjectController;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.DroppableGuiObjectController;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.GuiConnection;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.GuiPact;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionFocusEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactFocusEvent;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.Connection;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;

/**
 * Widget to design DAG for PactProgram - usually put into a tab panel
 */
public class GraphWidgetView extends Composite implements GraphWidgetViewInterface {
	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Reference to presenter - to access Model
	 */
	private GraphWidget graphWidget;

	/**
	 * UiBinder and its interface
	 */
	private static GraphUiViewBinder uiBinder = GWT.create(GraphUiViewBinder.class);

	public interface GraphUiViewBinder extends UiBinder<Widget, GraphWidgetView> {
	}

	/**
	 * Automatically assign attributes for items in menu bar
	 */
	@UiField
	TextButton button_newsource;
	@UiField
	TextButton button_newsink;
	@UiField
	TextButton button_newpact;
	@UiField
	TextButton button_newconnection;
	@UiField
	TextButton button_newjava;
	@UiField
	TextButton button_remove;
	@UiField
	TextButton button_edit;

	@UiField
	ButtonGroup createButtonGroup;

	@UiField
	ButtonGroup selectedButtonGroup;

	@UiField
	BorderLayoutContainer borderLayoutContainer;

	@UiField
	AbsolutePanel graphContainer;

	/**
	 * Currently focused widget (red highlighting)
	 */
	private DraggableGuiObject focusedWidget = null;

	/**
	 * Collection of GuiPacts (frontend representations of model pacts) within
	 * this graph widget
	 */
	private HashMap<Integer, GuiPact> guiPacts = new HashMap<Integer, GuiPact>();

	/**
	 * Collection of GuiConnection (frontend representations of model
	 * connections) within this graph widget
	 */
	private HashMap<Integer, GuiConnection> guiConnections = new HashMap<Integer, GuiConnection>();

	/**
	 * On drag controller to make all GuiConnectionHandler draggable
	 */
	private DraggableGuiObjectController guiObjectDragController;

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 * @param pactProgram
	 */
	@Inject
	public GraphWidgetView(AppInjector appInjector) {
		this.appInjector = appInjector;

		Log.debug(this + ": Constructor");

		this.initWidget(uiBinder.createAndBindUi(this));
		guiObjectDragController = new DraggableGuiObjectController(this);

		bind();
	}

	/**
	 * Views are injected via the dependency Injection Mechanism GIN and are
	 * mocked for testing with Mockito. Because both technics do not allow to
	 * pass additional parameters to the constructor, this method is usually
	 * called after constructing the view to pass additional data.
	 */
	@Override
	public void prepareWidget(GraphWidget graphWidget) {
		Log.debug(this + ": prepareWidget");

		this.graphWidget = graphWidget;

		// when graph widget is created due to loading of xml file
		// auto create pacts
		for (final Pact pact : graphWidget.getPactProgram().getPacts().values()) {
			addGuiPactForPact(pact);
		}

		// auto create connection
		for (final Connection connection : graphWidget.getPactProgram().getConnections().values()) {
			this.addOrGetGuiConnectionForConnection(connection);
		}

		guiConnections.get(null);

		resizeContainer();
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	public void bind() {
		Log.debug(this + ": bind");

		this.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				Log.debug("GuiPactProgramContainer: graphWidget.addResizeHandler");
				resizeContainer();
			}
		});
		
		graphContainer.addDomHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				Log.debug("Background click");
				handleClickOnConnectionOrBackground(event.getX(), event.getY());
			}
		}, MouseDownEvent.getType());
	}

	/**
	 * This method provided a way to add click event handlers from the presenter
	 * to this view
	 */
	@Override
	public void addClickEvent(ClickEventType clickEventTypeType, ClickHandler clickHandler) {
		switch (clickEventTypeType) {
		case BUTTON_NEW_PACT:
			button_newpact.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case BUTTON_NEW_SINK:
			button_newsink.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case BUTTON_NEW_SOURCE:
			button_newsource.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case BUTTON_NEW_CONNECTION:
			button_newconnection.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case BUTTON_NEW_JAVA:
			button_newjava.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case BUTTON_REMOVE:
			button_remove.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case BUTTON_EDIT:
			button_edit.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		default:
			Log.error(this + ": try to add unknown Click Event for " + clickEventTypeType);
			break;
		}
	}

	public HashMap<Integer, GuiConnection> getGuiConnections() {
		return guiConnections;
	}

	public HashMap<Integer, GuiPact> getGuiPacts() {
		return guiPacts;
	}

	public DraggableGuiObjectController getGuiObjectDragController() {
		return guiObjectDragController;
	}

	@Override
	public IsWidget getFocusWidget() {
		return focusedWidget;
	}

	/**
	 * The focus widget saves which line or pact has the focus - marked red
	 * 
	 * @param w
	 */
	public void setFocusWidget(DraggableGuiObject newFocusedWidget) {
		Log.debug(this + ": setFocusWidget " + newFocusedWidget);

		if (newFocusedWidget != focusedWidget) {
			DraggableGuiObject oldFocusedWidget = focusedWidget;
			focusedWidget = newFocusedWidget;

			if (oldFocusedWidget != null) {
				oldFocusedWidget.unfocus();
			}

			if (focusedWidget != null) {
				focusedWidget.focus();
				if (focusedWidget instanceof GuiPact) {
					appInjector.getEventBus().fireEvent(new PactFocusEvent(((GuiPact) focusedWidget).getPact()));
				} else if (focusedWidget instanceof GuiConnection) {
					appInjector.getEventBus().fireEvent(
							new ConnectionFocusEvent(((GuiConnection) focusedWidget).getConnection()));
				}
			}
			selectedButtonGroup.setEnabled(focusedWidget != null);
		}
	}

	public AbsolutePanel getGraphContainer() {
		return graphContainer;
	}

	/**
	 * Graph widget decides which connection was clicken when clicking on one of
	 * them
	 * 
	 * @param x
	 * @param y
	 */
	public void handleClickOnConnectionOrBackground(int x, int y) {
		Log.debug(this + ": handleClickOnConnectionOrBackground (x:" + x + ",y:" + y + ")");

		// iterate over all GuiConnections and find the closest to the
		// mouse click
		GuiConnection closestGuiConnectionToClick = null;
		double maximumDistanceOfGuiConnectionToClick = 15;
		for (Entry<Integer, GuiConnection> entry : guiConnections.entrySet()) {
			GuiConnection guiConnection = entry.getValue();
			Connection connection = guiConnection.getConnection();
			double distanceOfGuiConnectionToClick = guiConnection.calcDistanceToPoint(connection.getFromX(),
					connection.getFromY(), connection.getToX(), connection.getToY(), x, y);

			if (distanceOfGuiConnectionToClick < maximumDistanceOfGuiConnectionToClick) {
				maximumDistanceOfGuiConnectionToClick = distanceOfGuiConnectionToClick;
				closestGuiConnectionToClick = guiConnection;
			}
		}

		setFocusWidget(closestGuiConnectionToClick);
	}

	/**
	 * only refresh one connection
	 */
	public void refreshGuiConnections(Connection connection) {
		if (connection != null) {
			GuiConnection guiConnection = guiConnections.get(connection.getId());
			if (guiConnection != null) {
				guiConnection.refresh();
			}
		}
	}

	@Override
	public void refreshGuiConnectionsOfConnections(ArrayList<Connection> connections) {
		if (connections != null) {
			for (Connection connection : connections) {
				GuiConnection guiConnection = guiConnections.get(connection.getId());
				if (guiConnection != null) {
					guiConnection.refresh();
				}
			}
		}
	}

	/**
	 * Refresh all GuiConnections in his graphWidget
	 */
	@Override
	public void refreshGuiConnections(ArrayList<GuiConnection> guiConnectionsToRefresh) {
		if (guiConnectionsToRefresh != null && guiConnectionsToRefresh.size() > 0) {
			for (GuiConnection guiConnection : guiConnectionsToRefresh) {
				guiConnection.refresh();
			}
		}
	}

	/**
	 * WORAROUND: For some reason the graphContainer is not automatically
	 * resizing itself.
	 */
	public void resizeContainer() {
		Log.debug(this + ": resizeGraphContainer");

		// minimum padding between pact and border
		int paddingAroundBoxes = 50;

		// first force borderLayoutContainer do resize to maximum extension
		borderLayoutContainer.clearSizeCache();
		borderLayoutContainer.forceLayout();

		// get maximum x and y of element in graph
		int maxLeft = 0;
		int maxTop = 0;
		for (GuiConnection guiConnection : guiConnections.values()) {
			maxLeft = Math.max(maxLeft, guiConnection.getConnection().getFromX());
			maxLeft = Math.max(maxLeft, guiConnection.getConnection().getToX());

			maxTop = Math.max(maxTop, guiConnection.getConnection().getFromY());
			maxTop = Math.max(maxTop, guiConnection.getConnection().getToY());
		}
		for (GuiPact guiPact : guiPacts.values()) {
			maxLeft = Math.max(maxLeft, guiPact.getPact().getX() + guiPact.getWidth());
			maxTop = Math.max(maxTop, guiPact.getPact().getY() + guiPact.getHeight());
		}
		maxLeft = Math.max(maxLeft + paddingAroundBoxes, graphContainer.getElement().getParentElement()
				.getOffsetWidth());
		maxTop = Math
				.max(maxTop + paddingAroundBoxes, graphContainer.getElement().getParentElement().getOffsetHeight());

		// set to new size
		Log.debug(this + ": Resize to (w:" + maxLeft + ", h:" + maxTop + ")");
		graphContainer.setWidth(maxLeft + "px");
		graphContainer.setHeight(maxTop + "px");

		// don't know why this was needed before: refreshGuiPacts();
	}

	/**
	 * when pact was added to model - this method does the changes to the view;
	 * invoked by presenter
	 */
	@Override
	public void addGuiPactForPact(Pact pact) {
		GuiPact newGuiPact = null;
		if (pact != null && pact.getPactProgram() == graphWidget.getPactProgram()) {
			newGuiPact = guiPacts.get(pact.getId());
			if (newGuiPact == null) {
				newGuiPact = new GuiPact(appInjector, this, pact);
				graphContainer.add(newGuiPact, pact.getX(), pact.getY());
				guiPacts.put(pact.getId(), newGuiPact);
				Log.debug(this + ": GuiPact" + pact.getId() + " created");
			}

			if (newGuiPact != null) {
				setFocusWidget(newGuiPact);
			}
		}
	}

	/**
	 * when connection was added to model - this method does the changes to the
	 * view; invoked by presenter
	 */
	@Override
	public GuiConnection addOrGetGuiConnectionForConnection(Connection connection) {
		GuiConnection newGuiConnection = null;
		if (connection != null && connection.getPactProgram() == graphWidget.getPactProgram()) {
			newGuiConnection = guiConnections.get(connection.getId());
			if (newGuiConnection == null) {
				newGuiConnection = new GuiConnection(appInjector, this, connection);
				guiConnections.put(connection.getId(), newGuiConnection);
				Log.debug(this + ": guiConnections" + connection.getId() + " created");
				newGuiConnection.refresh();
			}
		}

		return newGuiConnection;
	}

	@Override
	public void registerDropController(DroppableGuiObjectController guiObjectInputDropController) {
		getGuiObjectDragController().registerDropController(guiObjectInputDropController);
	}

	@Override
	public void makeDraggable(GuiPact guiPact) {
		getGuiObjectDragController().makeDraggable(guiPact);
	}

	@Override
	public void unregisterDropController(DroppableGuiObjectController guiPactDropAreaController) {
		getGuiObjectDragController().unregisterDropController(guiPactDropAreaController);
	}

	public String toString() {
		return "GraphWidgetView ("
				+ (graphWidget == null ? "no program initialized so far" : "pactProgram:"
						+ graphWidget.getPactProgram().getName()) + ")";
	}
}
