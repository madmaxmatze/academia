package eu.stratosphere.pact.gui.designer.client.component.graph.object;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.user.client.ui.Widget;

/**
 * Drop controller for single GuiObject
 */
public class DroppableGuiObjectController extends SimpleDropController {
	/**
	 * Interface which needs to be implemented by all GuiObjects which are dropTargets
	 * 
	 * @author MathiasNitzsche@gmail.com
	 */
	public interface GuiDropInterface {
		void handleDrop(DraggableGuiObject dragObject);

		void handleDropEnter(DraggableGuiObject dragObject);

		void handleDropLeave(DraggableGuiObject dragObject);

		boolean isDropValid(DraggableGuiObject dragObject);
	}

	/**
	 * Constructor
	 * 
	 * @param dropTarget
	 *            : GuiObject for which this drop controller is created
	 */
	public DroppableGuiObjectController(GuiDropInterface dropTarget) {
		super((Widget) dropTarget);
	}

	/**
	 * Operation when entering DropObject with draggable
	 */
	@Override
	public void onEnter(DragContext context) {
		super.onEnter(context);
		getGuiObjectDropTarget().handleDropEnter(getDraggable(context));
	}

	/**
	 * Operation when leaving DropObject with draggable
	 */
	@Override
	public void onLeave(DragContext context) {
		super.onLeave(context);
		getGuiObjectDropTarget().handleDropLeave(getDraggable(context));
	}

	/**
	 * When dropping something, let GuiObject itself decide if drop is possible
	 */
	@Override
	public void onDrop(DragContext context) {
		super.onDrop(context);
		getGuiObjectDropTarget().handleDrop(getDraggable(context));
	}

	/**
	 * Helper to cast DropTarget (passed to cinstructor of GuiObjectDropController)
	 * @return
	 */
	private GuiDropInterface getGuiObjectDropTarget() {
		return (GuiDropInterface) getDropTarget();
	}

	/**
	 * Helper to cast draggable
	 * @return
	 */
	private DraggableGuiObject getDraggable(DragContext context) {
		return (DraggableGuiObject) context.draggable;
	}
}
