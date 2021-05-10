package eu.stratosphere.pact.gui.designer.client.component.graph.object;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.Composite;

import eu.stratosphere.pact.gui.designer.shared.model.Connection;

/**
 * GUI representation of a connection Actually only wraps start- and end-handler
 * and hold reference to the connection model object
 */
public class GuiConnectionCanvas extends Composite {
	/**
	 * length of line next to start and end handler
	 */
	static public int START_END_LINE_LENGTH = 15;

	/**
	 * Connection reference to the model
	 */
	private GuiConnection guiConnection;

	private Canvas canvas = Canvas.createIfSupported();

	/**
	 * Constructor is creating child widgets
	 */
	public GuiConnectionCanvas(GuiConnection guiConnection) {
		this.guiConnection = guiConnection;
		
		Log.debug(this + ": Constructor");
		
		this.initWidget(canvas);
	}

	/**
	 * propagate refresh operation to child widgets
	 */
	public void refresh() {
		// Log.debug(this + ": refresh");
		
		if (canvas != null) {
			Connection connection = guiConnection.getConnection();
		
			int startPosX = connection.getFromX();
			int startPosY = connection.getFromY();

			int endPosX = connection.getToX();
			int endPosY = connection.getToY();

			int padding = 20;
			int canvasLeft = Math.min(startPosX, endPosX) - padding;
			int canvasTop = Math.min(startPosY, endPosY) - padding;
			int canvasWidth = Math.abs(startPosX - endPosX) + padding * 2;
			int canvasHeight = Math.abs(startPosY - endPosY) + padding * 2;

			canvas.setCoordinateSpaceHeight(canvasHeight);
			canvas.setCoordinateSpaceWidth(canvasWidth);
			canvas.setPixelSize(canvasWidth, canvasHeight);

			this.getElement().setAttribute("style",
					"top: " + canvasTop + "px; left: " + canvasLeft + "px; position: absolute;");
			
			startPosX -= canvasLeft;
			startPosY -= canvasTop;
			endPosX -= canvasLeft;
			endPosY -= canvasTop;

			Context2d context = canvas.getContext2d();
			context.clearRect(0, 0, 1000, 1000);

			boolean isValidConnection = connection.isValid();
			context.setStrokeStyle(guiConnection.hasFocus() ? "red" : (isValidConnection ? "black" : "gray"));
			context.setFillStyle(guiConnection.hasFocus() ? "red" : (isValidConnection ? "black" : "gray"));
			context.setLineWidth(isValidConnection ? 2 : 1);

			// draw line
			context.beginPath();
			context.moveTo(startPosX, startPosY);
			context.lineTo(startPosX + START_END_LINE_LENGTH, startPosY);
			context.lineTo(endPosX - START_END_LINE_LENGTH, endPosY);
			context.lineTo(endPosX, endPosY);
			context.stroke();

			// draw arrow
			context.beginPath();
			context.moveTo(endPosX, endPosY);
			context.lineTo(endPosX - 10, endPosY + 5);
			context.lineTo(endPosX - 7, endPosY);
			context.lineTo(endPosX - 10, endPosY - 5);
			context.fill();
		}
	}
	
	public String toString() {
		return "GuiConnection(" + guiConnection.getConnection().getId() + ")-Canvas";
	}
}