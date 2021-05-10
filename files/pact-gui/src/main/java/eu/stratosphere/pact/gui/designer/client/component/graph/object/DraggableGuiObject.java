package eu.stratosphere.pact.gui.designer.client.component.graph.object;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;

/**
 * This Abstract Class serves as Base Class for all Draggable GUI Objects (like
 * Pacts, Sinks, ConntionHandler)
 * 
 * @author MathiasNitzsche@gmail.com
 */
public abstract class DraggableGuiObject extends FocusPanel {
	/**
	 * needed to fully support dragging
	 */
	abstract void handleDragStart();

	abstract void handleDragEnd();

	abstract void handleDragMove();

	abstract boolean isDragValid();

	/**
	 * just a few other methods which are not connected to dragging but are
	 * needed for other purposes
	 */
	public abstract void focus();

	public abstract void unfocus();

	public abstract void remove();

	public abstract void refresh();
	
	public abstract void removeWithQuestion();
	
	/**
	 * Reference to parent graph Widget
	 */
	public abstract AbsolutePanel getContainer();

	public int getX() {
		return this.getAbsoluteLeft() - getContainer().getAbsoluteLeft();
	}

	public int getY() {
		return this.getAbsoluteTop() - getContainer().getAbsoluteTop();
	}

	public void setX(int x) {
		this.getElement().getStyle().setLeft(x, Unit.PX);
	}

	public void setY(int y) {
		this.getElement().getStyle().setTop(y, Unit.PX);
	}
}
