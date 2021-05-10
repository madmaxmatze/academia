package eu.stratosphere.pact.gui.designer.client.component.graph.object;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;

import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidget.GraphWidgetViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.DroppableGuiObjectController.GuiDropInterface;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.GuiConnectionHandler.HandlerType;

/**
 * GuiPactDropAreas are widgets on the left and right of pacts. Here
 * GuiConnectionHandlers are dropped and later attached
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class GuiPactDropArea extends AbsolutePanel implements GuiDropInterface {
	GuiPact guiPact = null;
	GraphWidgetViewInterface graphWidget = null;

	/*
	 * is this GuiPactDropArea an input (left) or an output (right) Area
	 */
	static public enum AreaTypes {
		INPUT, OUTPUT
	}

	// multiple inputs to a pact a possible - numbered with this channelNumber
	// attribute
	private int channelNumber = 0;

	private AreaTypes areaType = AreaTypes.INPUT;

	/*
	 * Constuctor
	 */
	public GuiPactDropArea(GuiPact guiPact, GraphWidgetViewInterface graphWidgetViewInterface, AreaTypes areaType, int channelNumber) {
		super();
		
		this.guiPact = guiPact;
		this.graphWidget = graphWidgetViewInterface;
		this.areaType = areaType;
		this.channelNumber = channelNumber;

		Log.debug(this + ": Construtor");

		add(new HTML("&raquo;"));
		setStyleName("pactDropHandler");
		addStyleDependentName(areaType.toString());
	}

	/**
	 * Handler when any other GuiObject is dropped on this GuiPact
	 */
	@Override
	public void handleDrop(DraggableGuiObject dropObject) {
		Log.debug(this + ": handleDrop");

		guiPact.handleDrop(dropObject, channelNumber);
	}

	public AreaTypes getAreaType() {
		return areaType;
	}

	public int getChannelNumber() {
		return channelNumber;
	}

	/**
	 * When hovering this GuiPact with a GuiConnectionHander - colorize the
	 * GuiPact
	 */
	@Override
	public void handleDropEnter(DraggableGuiObject dropObject) {
		Log.debug(this + ": handleDropEnter");

		if (dropObject instanceof GuiConnectionHandler) {
			this.addStyleDependentName("drop" + isDropValid(dropObject));
		}
	}

	/**
	 * Reverse on enter operation
	 */
	@Override
	public void handleDropLeave(DraggableGuiObject dropObject) {
		Log.debug(this + ": handleDropLeave");

		this.removeStyleDependentName("droptrue");
		this.removeStyleDependentName("dropfalse");
	}

	/**
	 * Method is called when drag objects are dropped on this area. The parent
	 * GuiPact decides if this is allowed
	 */
	@Override
	public boolean isDropValid(DraggableGuiObject dragObject) {
		Log.debug(this + ": isDropValid");

		// only GuiConnectionHandler can be droped here
		GuiConnectionHandler guiConnectionHandler = null;
		if (dragObject instanceof GuiConnectionHandler) {
			guiConnectionHandler = (GuiConnectionHandler) dragObject;
		} else {
			return false;
		}

		// no start-Connector with inputPactArea
		// no end-Connector with outputPactArea
		if (guiConnectionHandler.getHandlerType() == HandlerType.START && this.areaType == AreaTypes.INPUT
				|| guiConnectionHandler.getHandlerType() == HandlerType.END && this.areaType == AreaTypes.OUTPUT) {
			return false;
		}

		return guiPact.isDropValid(dragObject);
	}

	public String toString() {
		return "GuiPact(" + guiPact.getPact().getId() + ")-" + areaType + "-DropArea";
	}
}
