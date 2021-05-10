package eu.stratosphere.pact.gui.designer.client.component.graph.object;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidgetView;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;

/**
 * Little drag and drop object at the end of connections. HandlerType defines if
 * start of end handler
 */
public class GuiConnectionHandler extends DraggableGuiObject {
	/**
	 * Handler size is defined here and not in css, because the size is needed
	 * for calculations like finding the center of the hander for the
	 * conntection arrow
	 */
	static public int HANDLER_SIZE = 18;
	static public int HANDLER_CONTENT_SIZE = 8;

	/**
	 * HandlerType defines if start of end handler
	 */
	private HandlerType handlerType;

	static public enum HandlerType {
		START, END
	}

	/**
	 * Reference to parent graph Widget
	 */
	private GuiConnection guiConnection;

	/**
	 * Reference to parent graph Widget
	 */
	private GraphWidgetView graphWidgetView;

	/**
	 * Constructor
	 * 
	 * @param graphWidgetView
	 * @param guiConnection
	 * @param handlerType
	 */
	public GuiConnectionHandler(GraphWidgetView graphWidgetView, GuiConnection guiConnection, HandlerType handlerType) {
		this.graphWidgetView = graphWidgetView;
		this.guiConnection = guiConnection;
		this.handlerType = handlerType;

		Log.debug(this + ": Constructor");

		this.setWidth(HANDLER_SIZE + "px");
		this.setHeight(HANDLER_SIZE + "px");

		// this css is set here because - see HANDLER_SIZE attribute comment
		FocusPanel content = new FocusPanel();
		Style contentStyle = content.getElement().getStyle();
		contentStyle.setWidth(HANDLER_CONTENT_SIZE, Unit.PX);
		contentStyle.setHeight(HANDLER_CONTENT_SIZE, Unit.PX);
		contentStyle.setMargin((HANDLER_SIZE - HANDLER_CONTENT_SIZE) / 2, Unit.PX);
		this.add(content);

		this.setStylePrimaryName("guiConnectionHandler");
		this.addStyleDependentName(handlerType.toString().toLowerCase());

		graphWidgetView.getGuiObjectDragController().makeDraggable(this);

		bind();

		refresh();
	}

	public void bind() {
		// little workaround, because otherwise the GuiPact is focused when
		// starting to drag a GuiConnectionHander (from within a
		// GuiPact-DropArea)
		this.addDomHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				event.stopPropagation();
				event.preventDefault();
			}
		}, MouseDownEvent.getType());
	}

	public HandlerType getHandlerType() {
		return handlerType;
	}

	/**
	 * When moving handler change actual model and refresh lines
	 * 
	 * @param context
	 */
	public void handleDragMove() {
		Log.debug(this + ": handleDragMove");
		int x = getX() + (int) Math.floor(HANDLER_SIZE / 2);
		int y = getY() + (int) Math.floor(HANDLER_SIZE / 2);

		if (handlerType == HandlerType.START) {
			guiConnection.getConnection().setFromX(x);
			guiConnection.getConnection().setFromY(y);
		} else {
			guiConnection.getConnection().setToX(x);
			guiConnection.getConnection().setToY(y);
		}

		guiConnection.refresh();
	}

	public GuiConnection getGuiConnection() {
		return guiConnection;
	}

	/**
	 * Refresh location and parent of handler - when loading a pactProgram and
	 * after drop
	 */
	public void refresh() {
		// Log.debug(this + ": refresh");

		// do nothing in case of drag&drop
		Widget containerWidget = this.getParent();
		if (containerWidget != null) {
			if ("dragdrop-movable-panel".equals(containerWidget.getStyleName())) {
				return;
			}
		}

		// append as child this-guiconnectionHandler either to main canvas or a
		// specific pact
		int x = 0;
		int y = 0;

		AbsolutePanel parentWidget = guiConnection.getCanvasWrapper(); // graphWidget.getMainPanel();
		if (handlerType == HandlerType.START) {
			Pact fromPact = guiConnection.getConnection().getFromPact();
			if (fromPact == null) {
				x = guiConnection.getConnection().getFromX() - (int) Math.floor(HANDLER_SIZE / 2);
				y = guiConnection.getConnection().getFromY() - (int) Math.floor(HANDLER_SIZE / 2);
			} else {
				parentWidget = (AbsolutePanel) graphWidgetView.getGuiPacts().get(fromPact.getId())
						.getParentWidgetForConnectionHandler(guiConnection);
			}

			parentWidget.add(this, x, y);
		} else {
			Pact toPact = guiConnection.getConnection().getToPact();
			if (toPact == null) {
				x = guiConnection.getConnection().getToX() - (int) Math.floor(HANDLER_SIZE / 2);
				y = guiConnection.getConnection().getToY() - (int) Math.floor(HANDLER_SIZE / 2);
			} else {
				parentWidget = (AbsolutePanel) graphWidgetView.getGuiPacts().get(toPact.getId())
						.getParentWidgetForConnectionHandler(guiConnection);
			}

			parentWidget.add(this, x, y);
		}
	}

	/**
	 * Change connection in model
	 * 
	 * @param channelNumber
	 */
	public void connectTo(GuiPact guiPact, int channelNumber) {
		Log.debug(this + ": ConnectTo " + guiPact + " channel" + channelNumber);

		Pact connectToPact = (guiPact == null ? null : guiPact.getPact());

		if (handlerType == GuiConnectionHandler.HandlerType.START) {
			guiConnection.getConnection().setFromPact(connectToPact);
		} else {
			guiConnection.getConnection().setToPact(connectToPact);
			guiConnection.getConnection().setToChannelNumber(channelNumber);
		}

		guiConnection.refresh();
	}

	public void handleDragStart() {
		Log.debug(this + ": handleDragStart");
		this.connectTo(null, 0);
		graphWidgetView.setFocusWidget(guiConnection);
	}

	@Override
	public boolean isDragValid() {
		return true;
	}

	@Override
	public void handleDragEnd() {
		Log.debug(this + ": handleDragEnd");
		guiConnection.refresh();
		graphWidgetView.resizeContainer();
	}

	@Override
	public void focus() {
		Log.debug(this + ": focus");
		this.addStyleDependentName("focus");
	}

	@Override
	public void unfocus() {
		Log.debug(this + ": unfocus");
		this.removeStyleDependentName("focus");
	}

	@Override
	public void removeWithQuestion() {
		// cant be delete on its own
	}

	@Override
	public void remove() {
		// cant be delete on its own
	}

	@Override
	public AbsolutePanel getContainer() {
		return graphWidgetView.getGraphContainer();
	}

	public String toString() {
		return "GuiConnection(" + guiConnection.getConnection().getId() + ")-" + handlerType + "-Handler";
	}
}