package eu.stratosphere.pact.gui.designer.client.component.graph.object;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.info.Info;

import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidgetView;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionFocusEvent;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionFocusEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionRemoveEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.Connection;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;

/**
 * GUI representation of a connection Actually only wraps start- and end-handler
 * and hold reference to the connection model object
 */
public class GuiConnection extends DraggableGuiObject {
	private GuiConnection self = this;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * length of line next to start and end handler
	 */
	static public int START_END_LINE_LENGTH = 15;

	/**
	 * Reference to parent graph Widget
	 */
	private GraphWidgetView graphWidgetView;

	/**
	 * Connection reference to the model
	 */
	private Connection connection;

	/**
	 * Start and end handler
	 */
	private GuiConnectionHandler startHandler;
	private GuiConnectionHandler endHandler;
	private GuiConnectionCanvas guiConnectionCanvas;

	/**
	 * Temp-variables to save old position during drag
	 */
	private int dragStartX;
	private int dragStartY;

	/**
	 * Every connection contains a canvas to draw the line. This Widgets wraps
	 * this canvas
	 */
	private AbsolutePanel canvasWrapper;

	/**
	 * Constructor is creating child widgets
	 * 
	 * @param appInjector
	 */
	public GuiConnection(AppInjector appInjector, GraphWidgetView graphWidgetView, Connection connection) {
		this.graphWidgetView = graphWidgetView;
		this.connection = connection;
		this.appInjector = appInjector;

		Log.debug(this + ": Constructor");

		graphWidgetView.getGraphContainer().add(this);
		graphWidgetView.getGuiObjectDragController().makeDraggable(this);

		// GuiObjectContentWithControls guiObjectContentWithControls = new
		// GuiObjectContentWithControls(this);
		AbsolutePanel guiObjectContentWithControls = new AbsolutePanel();
		guiObjectContentWithControls.setStylePrimaryName("guiObjectContent");

		guiConnectionCanvas = new GuiConnectionCanvas(this);
		this.setStylePrimaryName("guiConnection");

		canvasWrapper = new AbsolutePanel();
		canvasWrapper.add(guiConnectionCanvas);
		canvasWrapper.addStyleName("canvasWrapper");
		guiObjectContentWithControls.add(canvasWrapper);
		add(guiObjectContentWithControls);

		startHandler = new GuiConnectionHandler(graphWidgetView, this, GuiConnectionHandler.HandlerType.START);
		endHandler = new GuiConnectionHandler(graphWidgetView, this, GuiConnectionHandler.HandlerType.END);

		bind();
	}

	/**
	 * bind event handlers related to a GuiConnection
	 */
	private void bind() {
		Log.debug(this + ": bind");

		// click on a connection might not select this connection, because of
		// crossing lines
		guiConnectionCanvas.addDomHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				Log.debug(this + ": mouse click");

				if (event.getNativeButton() == 1) { // left click
					graphWidgetView.handleClickOnConnectionOrBackground(getX() + event.getX(), getY() + event.getY());
				}
				if (event.getNativeButton() == 2) { // right click

				}
			}
		}, MouseDownEvent.getType());

		// focus the GuiConnection in Graph (usually after a GuiConnection was
		// clicked in the tree on the left)
		appInjector.getEventBus().addHandler(ConnectionFocusEvent.TYPE, new ConnectionFocusEventHandler() {
			@Override
			public void onConnectionFocus(ConnectionFocusEvent event) {
				if (event.getConnection() == connection) {
					Log.debug(this + ": Focus connection Event catched");
					graphWidgetView.setFocusWidget(self);
				}
			}
		});

		appInjector.getEventBus().addHandler(ConnectionRemoveEvent.TYPE, new ConnectionRemoveEventHandler() {
			@Override
			public void onConnectionRemove(ConnectionRemoveEvent event) {
				if (event.getConnection() == connection) {
					if (hasFocus()) {
						graphWidgetView.setFocusWidget(null);
					}
					graphWidgetView.getGuiConnections().remove(connection.getId());
					self.getStartHandler().removeFromParent();
					self.getEndHandler().removeFromParent();
					self.removeFromParent();
				}
			}
		});

		// only make connection draggable if both drag handler are unconnected
		this.addDomHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				if (connection.getFromPact() != null || connection.getToPact() != null) {
					// Log.debug(this +
					// ": onMouseMove -> set draggingImpossible");
					self.addStyleDependentName("draggingImpossible");
				}
			}
		}, MouseMoveEvent.getType());

		// remove added class from MouseMoveEvent
		this.addDomHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				Log.debug(this + ": onMouseOut");
				self.removeStyleDependentName("draggingImpossible");
			}
		}, MouseOutEvent.getType());
	}

	/**
	 * Get Model behind this gui connection widget
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * refresh position and refresh handler / canvas (redraw line)
	 */
	public void refresh() {
		Log.debug(this + ": refresh (including canvas and END-handlers)");

		// set x and y of from-connection-handler based on from pact
		GuiPact fromGuiPact = getFromGuiPact();
		if (fromGuiPact != null) {
			Pact fromPact = fromGuiPact.getPact();
			connection.setFromX(fromPact.getX() + fromGuiPact.getWidth(), false);
			connection.setFromY(fromPact.getY() + 30, false);
		}

		// set x and y of to-connection-handler based on to pact
		GuiPact toGuiPact = getToGuiPact();
		if (toGuiPact != null) {
			Pact toPact = toGuiPact.getPact();
			connection.setToX(toPact.getX(), false);
			connection.setToY(toPact.getY() + 30 + 30 * connection.getToChannelNumber(), false);
		}

		int startPosX = connection.getFromX();
		int startPosY = connection.getFromY();

		int endPosX = connection.getToX();
		int endPosY = connection.getToY();

		int padding = 20;
		int canvasLeft = Math.min(startPosX, endPosX) - padding;
		int canvasTop = Math.min(startPosY, endPosY) - padding;
		int canvasWidth = Math.abs(startPosX - endPosX) + padding * 2;
		int canvasHeight = Math.abs(startPosY - endPosY) + padding * 2;

		// when not draggen in the moment
		if (this.getParent() == graphWidgetView.getGraphContainer()) {
			canvasWrapper.getElement().setAttribute("style",
					"top: -" + canvasTop + "px; left: -" + canvasLeft + "px; position: absolute;");
			this.getElement().setAttribute("style",
					"top: " + canvasTop + "px; left: " + canvasLeft + "px; position: absolute;");
			this.setHeight(canvasHeight + "px");
			this.setWidth(canvasWidth + "px");
		}

		guiConnectionCanvas.refresh();
		startHandler.refresh();
		endHandler.refresh();
	}

	/**
	 * Open confirm dialog to ask if this GuiConnection should be deleted
	 */
	public void removeWithQuestion() {
		Log.debug(this + ": removeWithQuestion");

		ConfirmMessageBox box = new ConfirmMessageBox("Confirm Deletion",
				"Are you sure you want to delete this connection (id:" + connection.getId() + ")?");
		box.addHideHandler(new HideHandler() {
			@Override
			public void onHide(HideEvent event) {
				Dialog btn = (Dialog) event.getSource();
				if (btn.getHideButton().getText().equalsIgnoreCase(String.valueOf(PredefinedButton.YES))) {
					remove();
					Info.display("Deleted", "Connection deleted");
				} else {
					Info.display("Not deleted", "Connection not deleted");
				}
			}
		});
		box.show();
	}

	/**
	 * Needed to change start and end handler color
	 */
	public void unfocus() {
		Log.debug(this + ": unfocus");

		// this.setFocus(false);
		this.removeStyleDependentName("focus");
		startHandler.unfocus();
		endHandler.unfocus();
		refresh();
	}

	/**
	 * Needed to change start and end handler color
	 */
	public void focus() {
		Log.debug(this + ": focus");

		// this.setFocus(true);
		this.addStyleDependentName("focus");
		startHandler.focus();
		endHandler.focus();
		refresh();
	}

	/**
	 * Self delete
	 */
	public void remove() {
		Log.debug(this + ": remove");
		connection.remove();
	}

	/**
	 * get Gui object the connection is coming from
	 */
	public GuiPact getFromGuiPact() {
		Pact fromPact = connection.getFromPact();
		if (fromPact != null) {
			return graphWidgetView.getGuiPacts().get(fromPact.getId());
		}

		return null;
	}

	/**
	 * get Gui object the connection is pointing to
	 */
	public GuiPact getToGuiPact() {
		Pact toPact = connection.getToPact();
		if (toPact != null) {
			return graphWidgetView.getGuiPacts().get(toPact.getId());
		}

		return null;
	}

	public GuiConnectionHandler getStartHandler() {
		return startHandler;
	}

	public GuiConnectionHandler getEndHandler() {
		return endHandler;
	}

	/**
	 * Save x and y of GuiConnection when drag start
	 */
	public void handleDragStart() {
		Log.debug(this + ": handleDragStart");

		graphWidgetView.setFocusWidget(this);
		dragStartX = getX();
		dragStartY = getY();
	}

	@Override
	public void handleDragMove() {

	}

	/**
	 * set new gui connection position
	 */
	@Override
	public void handleDragEnd() {
		Log.debug(this + ": handleDragEnd");

		int divX = getX() - dragStartX;
		int divY = getY() - dragStartY;

		connection.setFromX(connection.getFromX() + divX);
		connection.setFromY(connection.getFromY() + divY);
		connection.setToX(connection.getToX() + divX);
		connection.setToY(connection.getToY() + divY);
		refresh();
	}

	public boolean isDragValid() {
		GuiPact fromGuiPact = getFromGuiPact();
		GuiPact toGuiPact = getToGuiPact();
		boolean isValid = (fromGuiPact == null && toGuiPact == null);

		Log.debug(this + ": isDragValid=" + isValid);

		return isValid;
	}

	public boolean hasFocus() {
		return (this == graphWidgetView.getFocusWidget());
	}

	/**
	 * Helper method to calculate smallest distance between line segment and dot
	 * (taken from http://www.desert.cx/node/11)
	 */
	public double calcDistanceToPoint(int startX, int startY, int endX, int endY, int clickX, int clickY) {
		Log.debug(this + ": calcDistanceToPoint");

		double dx = endX - startX;
		double dy = endY - startY;
		double t = -((startX - clickX) * dx + (startY - clickY) * dy) / ((dx * dx) + (dy * dy));

		if (t < 0.0) {
			t = 0.0;
		} else if (t > 1.0) {
			t = 1.0;
		}

		dx = (startX + t * (endX - startX)) - clickX;
		dy = (startY + t * (endY - startY)) - clickY;
		double rt = Math.sqrt((dx * dx) + (dy * dy));

		return rt;
	}

	/**
	 * To rotate the arrow, the angle of the line is important NOT USED IN THE
	 * MOMENT
	 * 
	 * @param startPosX
	 * @param startPosY
	 * @param endPosX
	 * @param endPosY
	 * @return
	 */
	@SuppressWarnings("unused")
	private double calcAngle(int startPosX, int startPosY, int endPosX, int endPosY) {
		Log.debug(this + ": calcAngle");

		int width = Math.abs(startPosX - endPosX);
		int height = Math.abs(startPosY - endPosY);

		double degree = 180 / Math.PI * Math.atan((double) height / (double) (width == 0 ? 1 : width));
		degree = (startPosY > endPosY ? (startPosX < endPosX ? degree : 180 - degree)
				: (startPosX > endPosX ? 180 + degree : 360 - degree));
		return degree;
	}

	public AbsolutePanel getCanvasWrapper() {
		return canvasWrapper;
	}

	@Override
	public AbsolutePanel getContainer() {
		return graphWidgetView.getGraphContainer();
	}

	public String toString() {
		return "GuiConnection(" + getConnection().getId() + ")";
	}
}