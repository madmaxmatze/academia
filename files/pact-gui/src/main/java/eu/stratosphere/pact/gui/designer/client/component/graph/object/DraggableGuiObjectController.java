package eu.stratosphere.pact.gui.designer.client.component.graph.object;

import com.allen_sauer.gwt.dnd.client.PickupDragController;

import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidgetView;

/**
 * Drag controller for all DragableGuiObjects
 */
public class DraggableGuiObjectController extends PickupDragController {
	/**
	 * Constructor
	 */
	public DraggableGuiObjectController(GraphWidgetView graphWidgetView) {
		// first parameter defines the drag border (BoundaryPanel)
		super(graphWidgetView.getGraphContainer(), true);

		this.setConstrainWidgetToBoundaryPanel(true);

		// start dragging after element has been moved by x Pixel
		this.setBehaviorDragStartSensitivity(5);
	}

	/**
	 * When start dragging a GuiConnectionHandler, it cannot be connected to any
	 * pact anymore
	 */
	@Override
	public void dragStart() {
		if (getDraggable().isDragValid()) {
			super.dragStart();
			getDraggable().handleDragStart();
		}
	}

	/**
	 * Callback on end of drag
	 */
	@Override
	public void dragEnd() {
		super.dragEnd();
		getDraggable().handleDragEnd();
	}

	/**
	 * Callback on drag move
	 */
	@Override
	public void dragMove() {
		super.dragMove();
		getDraggable().handleDragMove();
	}

	/**
	 * Helper to cast DropTarget (passed to cinstructor of
	 * GuiObjectDropController)
	 * 
	 * @return
	 */
	private DraggableGuiObject getDraggable() {
		return (DraggableGuiObject) context.draggable;
	}
}
