package eu.stratosphere.pact.gui.designer.client.component.graph.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

import eu.stratosphere.pact.gui.designer.client.component.graph.GraphWidget.GraphWidgetViewInterface;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.DroppableGuiObjectController.GuiDropInterface;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.GuiConnectionHandler.HandlerType;
import eu.stratosphere.pact.gui.designer.client.component.graph.object.GuiPactDropArea.AreaTypes;
import eu.stratosphere.pact.gui.designer.client.event.GlobalKeyPressEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactFocusEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactFocusEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactRemoveEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.UserFunctionOpenEvent;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.Connection;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactOutputType;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;

/**
 * Front end representation of pact model. Right now also represents sources and
 * sinks. At a later point in time maybe change to inheritance of base GuiObject
 */
public class GuiPact extends DraggableGuiObject implements GuiDropInterface {
	private GuiPact self = this;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Reference to parent graph Widget
	 */
	private GraphWidgetViewInterface graphWidgetView;

	/**
	 * Reference to model
	 */
	private Pact pact;

	/**
	 * All Drop areas in a pact
	 */
	private HashMap<String, GuiPactDropArea> guiPactDropAreas = new HashMap<String, GuiPactDropArea>();

	/**
	 * Reference to drop controller for this guiPact, to later unregister it,
	 * when deleting this pact
	 * 
	 * TODO: TEST IF REALLY NEEDED. GC schould take care of this when deleting
	 * this
	 */
	private HashMap<String, DroppableGuiObjectController> guiPactDropAreaControllers = new HashMap<String, DroppableGuiObjectController>();

	/**
	 * Pact Name in the middle of the guiPact Object
	 */
	private Label pactNameLabel;

	/**
	 * Pact Context Menu
	 */
	private Menu pactContextMenu = new Menu();
	private MenuItem pactContextMenuItemRemove = new MenuItem();
	private MenuItem pactContextMenuItemEdit = new MenuItem();
	private MenuItem pactContextMenuItemType = new MenuItem();

	/**
	 * Each Conext menu Item wraps a reference to a PactType
	 * 
	 * @author MathiasNitzsche@gmail.com
	 */
	class PactTypeMenuItem extends MenuItem {
		private PactType pactType;

		public PactType getPactType() {
			return pactType;
		}

		PactTypeMenuItem(PactType pactType) {
			super();
			this.pactType = pactType;
			setText(pactType.getLabel());
		}
	}

	/**
	 * Output contract label below pact name (empty of no output contract
	 * defined)
	 */
	private Label outputContractsLabel = new Label();

	/**
	 * control buttons on top right of guiPact object
	 */
	private ToolButton editButton = new ToolButton(ToolButton.GEAR);
	private ToolButton closeButton = new ToolButton(ToolButton.CLOSE);

	/**
	 * Wrapper for actual content - provides control buttons
	 */
	private ContentPanel guiPactFrame = new ContentPanel();

	/**
	 * inner element - needed for absolute positioning in wrapper
	 */
	private AbsolutePanel guiPactContent = new AbsolutePanel();

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 * 
	 * @param graphWidgetViewInterface
	 * @param pact
	 */
	public GuiPact(final AppInjector appInjector, GraphWidgetViewInterface graphWidgetViewInterface, final Pact pact) {
		super();

		this.appInjector = appInjector;
		this.graphWidgetView = graphWidgetViewInterface;
		this.pact = pact;

		Log.debug(this + ": Constructor");

		// this.getElement().setId("pact" + pact.getId());
		// this.addStyleDependentName(pact.getType().toString().toLowerCase());
		this.setStylePrimaryName("guiPact");

		// pact frame
		guiPactFrame.setHeight(500);
		guiPactFrame.setShadow(false);
		// top border not hidden:
		// http://www.sencha.com/forum/showthread.php?160291-FramedPanel-setHeaderBisible-does-not-work
		guiPactFrame.setBodyBorder(false);
		guiPactFrame.getHeader().setBorders(false);

		// type context menu
		pactContextMenuItemType.setHTML("Type");
		Menu typeSubMenu = new Menu();
		for (PactType pactType : PactType.getInputContractTypes()) {
			typeSubMenu.add(new PactTypeMenuItem(pactType));
		}
		pactContextMenuItemType.setSubMenu(typeSubMenu);
		pactContextMenu.add(pactContextMenuItemType);

		// edit context menu
		pactContextMenuItemEdit = new MenuItem();
		pactContextMenuItemEdit.setHTML("Edit");
		pactContextMenu.add(pactContextMenuItemEdit);

		// remove context menu
		pactContextMenuItemRemove = new MenuItem();
		pactContextMenuItemRemove.setHTML("Remove");
		pactContextMenu.add(pactContextMenuItemRemove);

		guiPactFrame.setContextMenu(pactContextMenu);

		// edit button on top right
		ToolTipConfig ttc = new ToolTipConfig("Edit "
				+ (pact.getType().isRealInputContract() ? "PACT" : pact.getType().getLabel()));
		ttc.setShowDelay(0);
		editButton.setToolTipConfig(ttc);
		guiPactFrame.getHeader().addTool(editButton);

		// remove button on top right
		ToolTipConfig closeButtonConfig = new ToolTipConfig("Remove "
				+ (pact.getType().isRealInputContract() ? "PACT" : pact.getType().getLabel()));
		closeButtonConfig.setShowDelay(0);
		closeButton.setToolTipConfig(closeButtonConfig);
		guiPactFrame.getHeader().addTool(closeButton);
		this.add(guiPactFrame);

		guiPactFrame.add(guiPactContent);
		guiPactContent.setStylePrimaryName("guiObjectContent");

		// WORKAROUND when this pact has focus to redirekt key press to global
		// key press event
		guiPactContent.addHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				self.appInjector.getEventBus().fireEvent(new GlobalKeyPressEvent(event));
			}
		}, KeyDownEvent.getType());

		pactNameLabel = new Label();
		pactNameLabel.addStyleName("pactNameLabel");
		guiPactContent.add(pactNameLabel);

		outputContractsLabel.addStyleName("pactOutputContractLabel");
		guiPactContent.add(outputContractsLabel);

		// output
		GuiPactDropArea guiOutputDropArea = new GuiPactDropArea(this, graphWidgetViewInterface, AreaTypes.OUTPUT, 0);
		guiPactContent.add(guiOutputDropArea, getWidth() - 20, 0);
		guiPactDropAreas.put("output", guiOutputDropArea);

		DroppableGuiObjectController guiObjectOutputDropController = new DroppableGuiObjectController(guiOutputDropArea);
		graphWidgetViewInterface.registerDropController(guiObjectOutputDropController);
		guiPactDropAreaControllers.put("output", guiObjectOutputDropController);

		// input
		for (int i = 0; i < 2; i++) {
			GuiPactDropArea guiInputDropArea = new GuiPactDropArea(this, graphWidgetViewInterface, AreaTypes.INPUT, i);
			guiPactContent.add(guiInputDropArea, 0, i * 30);
			guiPactDropAreas.put("input" + i, guiInputDropArea);

			DroppableGuiObjectController guiObjectInputDropController = new DroppableGuiObjectController(
					guiInputDropArea);
			graphWidgetViewInterface.registerDropController(guiObjectInputDropController);
			guiPactDropAreaControllers.put("input" + i, guiObjectInputDropController);
		}

		DroppableGuiObjectController guiObjectDropController = new DroppableGuiObjectController(this);
		graphWidgetViewInterface.registerDropController(guiObjectDropController);
		guiPactDropAreaControllers.put("guiPact", guiObjectDropController);

		// fill fields
		refresh();

		// make gui-pact itself draggable
		graphWidgetViewInterface.makeDraggable(this);

		bind();
	}

	/**
	 * Bind handler to this guiPact
	 */
	private void bind() {
		Log.debug(this + ": bind");

		/**
		 * click on close button on the top right of GuiPact
		 */
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": click on header close button");
				removeWithQuestion();
			}
		});

		/**
		 * click on settings button on the top right of GuiPact
		 */
		editButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": click on header edit button");
				appInjector.getEventBus().fireEvent(new UserFunctionOpenEvent(pact));
			}
		});

		/**
		 * select new pact type via context menu of GuiPact
		 */
		pactContextMenuItemType.getSubMenu().addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				Log.debug(self + ": click on context menu type submenu");
				pact.setType(((PactTypeMenuItem) event.getSelectedItem()).getPactType());
			}
		});

		/**
		 * click on remove in conext menu of GuiPact
		 */
		pactContextMenuItemRemove.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				Log.debug(self + ": click on context menu Remove item");
				removeWithQuestion();
			}
		});

		/**
		 * click on edit in conext menu of GuiPact
		 */
		pactContextMenuItemEdit.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				Log.debug(self + ": click on context menu Edit item");
				appInjector.getEventBus().fireEvent(new UserFunctionOpenEvent(pact));
			}
		});

		/**
		 * click on GuiPact to set focus
		 */
		this.addDomHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				Log.debug(self + ": click");
				graphWidgetView.setFocusWidget(self);
				event.stopPropagation();
				event.preventDefault();
			}
		}, MouseDownEvent.getType());

		/**
		 * double click on guiPact header
		 */
		guiPactFrame.getHeader().addHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				Log.debug(self + ": header DoubleClick");
				appInjector.getEventBus().fireEvent(new UserFunctionOpenEvent(pact));
			}
		}, DoubleClickEvent.getType());

		/**
		 * When pact properties have been changed (via java-editor tab) - update
		 * this gui Pact in the graph
		 */
		appInjector.getEventBus().addHandler(PactPropertyChangeEvent.TYPE, new PactPropertyChangeEventHandler() {
			@Override
			public void onPactPropertyChange(PactPropertyChangeEvent event) {
				if (event.getPact() == pact) {
					Log.debug(self + ": PactPropertyChangeEvent catched");

					// asure that even with the changed pact properties the pact
					// program is still valid
					pact.getPactProgram().refresh();

					// because of a different input contract type, some
					// connection might got disconnected - refresh connection
					graphWidgetView.refreshGuiConnectionsOfConnections(pact.getAllConnections());

					refresh();
				}
			}
		});

		// focus the GuiPact in Graph (usually after a pact was selected in the
		// tree on the left)
		appInjector.getEventBus().addHandler(PactFocusEvent.TYPE, new PactFocusEventHandler() {
			@Override
			public void onPactFocus(PactFocusEvent event) {
				if (event.getPact() == pact) {
					Log.debug(self + ": Focus event catched");
					graphWidgetView.setFocusWidget(self);
				}
			}
		});

		appInjector.getEventBus().addHandler(PactRemoveEvent.TYPE, new PactRemoveEventHandler() {
			@Override
			public void onPactRemove(PactRemoveEvent event) {
				if (event.getPact() == pact) {
					if (hasFocus()) {
						graphWidgetView.setFocusWidget(null);
					}

					graphWidgetView.getGuiPacts().remove(pact.getId());

					// update sink or source number oif other pacts
					if (pact.getType().isSinkOrSource()) {
						HashMap<Integer, Pact> sinkOrSourcePacts = pact.getPactProgram().getPactsByType(pact.getType());
						for (Pact p : sinkOrSourcePacts.values()) {
							GuiPact guiPact = graphWidgetView.getGuiPacts().get(p.getId());
							if (guiPact != null) {
								guiPact.refresh();
							}
						}
					}

					// remove drop handler for guiPact - need to be done before
					// removeFromParent()
					for (DroppableGuiObjectController guiPactDropAreaController : guiPactDropAreaControllers.values()) {
						graphWidgetView.unregisterDropController(guiPactDropAreaController);
					}

					self.removeFromParent();
				}
			}
		});

	}

	/**
	 * refresh graphic appearance of gui pact (position, title, type...)
	 */
	public void refresh() {
		Log.debug(this + ": refresh");

		this.setWidth(getWidth() + "px");
		this.setHeight(getHeight() + "px");
		this.setX(pact.getX());
		this.setY(pact.getY());

		// update guiPact headline text
		if (guiPactFrame != null && pact.getType().getLabel() != guiPactFrame.getHeader().getText()) {
			String displayType = pact.getType().getLabel()
					+ (pact.getType().isSinkOrSource() ? " #" + pact.getSourceOrSinkNumber() : "");
			guiPactFrame.setHeadingText(displayType);
		}

		// update Name of Pact Gui in content area
		if (pactNameLabel != null && pact.getName() != pactNameLabel.getText()) {
			pactNameLabel.setText(pact.getName());
			pactNameLabel.setTitle(pact.getName());
		}

		// update list of outputContracts (not used currently)
		if (outputContractsLabel != null) {
			ArrayList<PactOutputType> outputContracts = pact.getOutputContracts();
			outputContractsLabel.setText(outputContracts.size() == 0 ? "" : "" + outputContracts);
		}

		// show / hide GuiPactDropAreas according to pactType (Map, Cross...)
		for (Entry<String, GuiPactDropArea> e : guiPactDropAreas.entrySet()) {
			GuiPactDropArea guiPactDropArea = e.getValue();
			if (guiPactDropArea.getAreaType() == GuiPactDropArea.AreaTypes.INPUT) {
				guiPactDropArea.setVisible(guiPactDropArea.getChannelNumber() < pact.getType().getNumberOfInputs());
			} else {
				guiPactDropArea.setVisible(guiPactDropArea.getChannelNumber() < pact.getType().getNumberOfOutputs());
			}
		}

		// If this GuiPact is not a real PACT Contract (match, map..) then hide
		// pactType submenu in the context menu of the guiPact.
		if (pactContextMenuItemType != null) {
			pactContextMenuItemType.setVisible(pact.getType().isRealInputContract());
		}
	}

	/**
	 * Open confirm dialog to ask if this GuiPact should be deleted
	 */
	public void removeWithQuestion() {
		Log.debug(this + ": removeWithQuestion");

		ConfirmMessageBox box = new ConfirmMessageBox("Confirm Deletion",
				"Are you sure you want to delete the selected " + pact.getType().getLabel() + " (id:" + pact.getId()
						+ ")?");
		box.addHideHandler(new HideHandler() {
			@Override
			public void onHide(HideEvent event) {
				Dialog btn = (Dialog) event.getSource();
				if (btn.getHideButton().getText().equalsIgnoreCase(String.valueOf(PredefinedButton.YES))) {
					remove();
					Info.display("Deleted", "Object deleted");
				} else {
					Info.display("Not deleted", "Object not deleted");
				}
			}
		});
		box.show();
	}

	/**
	 * Delete this gui Pact, in response to pact model removement
	 */
	public void remove() {
		// remove from model
		Log.debug(this + ": remove");
		pact.remove();
	}

	/**
	 * Draging of pacts is allways possible
	 */
	@Override
	public boolean isDragValid() {
		return true;
	}

	/**
	 * During drag&drop of GuiPact the conntected GuiConnections have to be
	 * updated. This array temporarly holds these GuiConnections
	 */
	ArrayList<GuiConnection> currentGuiConnectionsDuringDrag;

	/**
	 * During drag&drop of GuiPact the conntected GuiConnections have to be
	 * updated. These GuiConnections are calculated during drag start
	 */
	@Override
	public void handleDragStart() {
		Log.debug(this + ": handleDragStart");

		// save guiConnection for this pact
		ArrayList<Connection> connectionsOfPact = pact.getInputConntections();
		connectionsOfPact.addAll(pact.getOutputConntections());

		currentGuiConnectionsDuringDrag = new ArrayList<GuiConnection>();
		for (Connection c : connectionsOfPact) {
			Log.debug(this + ": add for dragRefresh GuiConnection" + c.getId());
			currentGuiConnectionsDuringDrag.add(graphWidgetView.getGuiConnections().get(c.getId()));
		}

	}

	/**
	 * when moved change x and y in pact model
	 * 
	 * @param context
	 */
	public void handleDragMove() {
		Log.debug(this + ": handleDragMove");

		pact.setX(getX(), false);
		pact.setY(getY(), false);

		graphWidgetView.refreshGuiConnections(currentGuiConnectionsDuringDrag);
	}

	@Override
	public void handleDragEnd() {
		Log.debug(this + ": handleDragEnd");

		graphWidgetView.resizeContainer();
		// nothing to do here
	}

	@Override
	public void handleDrop(DraggableGuiObject dragObject) {
		handleDrop(dragObject, 0);
	}

	public void handleDrop(DraggableGuiObject dragObject, int channelNumber) {
		Log.debug(this + ": handleDrop of " + dragObject + " to channel " + channelNumber);

		if (isDropValid(dragObject)) {
			// first save dropObject from deletion, because it's
			// currently a child of dragdrop-movable-panel
			dragObject.removeFromParent();

			// connect guiConnection with guiPact
			((GuiConnectionHandler) dragObject).connectTo(this, channelNumber);
		} else {
			// if not valid, insert in container at the same position
			getContainer().add(dragObject, dragObject.getX(), dragObject.getY());
		}
	}

	/**
	 * if dropEnter with guiConntectionHandler on GuiPact - handle drop by
	 * appropiate guiPactDropArea
	 */
	@Override
	public void handleDropEnter(DraggableGuiObject dropObject) {
		Log.debug(this + ": handleDropEnter");

		if (dropObject instanceof GuiConnectionHandler) {
			GuiPactDropArea guiPactDropArea = guiPactDropAreas.get((((GuiConnectionHandler) dropObject)
					.getHandlerType() == HandlerType.END ? "input0" : "output"));
			if (guiPactDropArea != null) {
				guiPactDropArea.handleDropEnter(dropObject);
			}
		}
	}

	/**
	 * remove all highlights
	 */
	@Override
	public void handleDropLeave(DraggableGuiObject dropObject) {
		Log.debug(this + ": handleDropLeave");

		for (GuiPactDropArea guiPactDropArea : guiPactDropAreas.values()) {
			guiPactDropArea.handleDropLeave(dropObject);
		}
	}

	@Override
	public void focus() {
		Log.debug(this + ": focus");

		// set focus creates unstylable browserspecific focus (in crome dotted
		// gray line)
		// this.setFocus(true);
		// graphWidget.getContainerBackground().setFocus(true);
		this.addStyleDependentName("focus");
		refresh();
	}

	@Override
	public void unfocus() {
		Log.debug(this + ": unfocus");

		// this.setFocus(false);
		this.removeStyleDependentName("focus");

		refresh();
	}

	public boolean hasFocus() {
		return (this == graphWidgetView.getFocusWidget());
	}

	/**
	 * The current dropArea of a pact decides which drop is valid
	 * 
	 * @param guiConnectionHandler
	 * @return
	 */
	public boolean isDropValid(DraggableGuiObject dragObject) {
		Log.debug(this + ": isDropValid");

		// only GuiConnectionHandler can be droped here
		GuiConnectionHandler guiConnectionHandler = null;
		if (dragObject instanceof GuiConnectionHandler) {
			guiConnectionHandler = (GuiConnectionHandler) dragObject;
		} else {
			return false;
		}

		// no outgoing connection from sink
		if (guiConnectionHandler.getHandlerType() == HandlerType.START && pact.getType() == PactType.SINK) {
			return false;
		}

		// no incoming connection to source sink
		if (guiConnectionHandler.getHandlerType() == HandlerType.END && pact.getType() == PactType.SOURCE) {
			return false;
		}

		Pact toPact = guiConnectionHandler.getGuiConnection().getConnection().getToPact();
		Pact fromPact = guiConnectionHandler.getGuiConnection().getConnection().getFromPact();

		// start and end of connection can't be the same GuiPact
		if (fromPact == pact || toPact == pact) {
			return false;
		}

		// no cyles in DAG
		HashMap<Integer, Pact> pactSuccessors = pact.getSuccessors();
		Log.info("pactSuccessors" + pactSuccessors);
		if (guiConnectionHandler.getHandlerType() == HandlerType.START && toPact != null) {
			if (toPact.getSuccessors().containsValue(pact)) {
				return false;
			}
		}
		if (guiConnectionHandler.getHandlerType() == HandlerType.END && fromPact != null) {
			if (pactSuccessors.containsValue(fromPact)) {
				return false;
			}
		}

		// do double connections between a pact and all its successors
		// TODO: Maybe the pact model allows connection to far successors, but
		// not direct children.
		// in that case: fix with getSuccessors(int deep) parameter
		if (toPact != null && pactSuccessors.containsValue(toPact)) {
			return false;
		}
		if (fromPact != null && fromPact.getSuccessors().containsValue(pact)) {
			return false;
		}

		// so everything seems valid
		return true;
	}

	public GuiPactDropArea getParentWidgetForConnectionHandler(GuiConnection guiConnection) {
		Connection connection = guiConnection.getConnection();

		if (pact == connection.getFromPact()) {
			return guiPactDropAreas.get("output");
		} else {
			return guiPactDropAreas.get("input" + connection.getToChannelNumber());
		}
	}

	@Override
	public AbsolutePanel getContainer() {
		return graphWidgetView.getGraphContainer();
	}

	/**
	 * dynamically return width depending on PactType
	 */
	public int getWidth() {
		return (pact.getType().isRealInputContract() ? 150 : 150);
	}

	/**
	 * dynamically return height depending on PactType
	 */
	public int getHeight() {
		if (pact.getType().isRealInputContract()) {
			return 20 + Math.max(pact.getType().getNumberOfInputs(), pact.getType().getNumberOfOutputs()) * 30;
		}

		return 50;
	}

	/**
	 * Get model behind this GuiPact
	 * 
	 * @return
	 */
	public Pact getPact() {
		return pact;
	}

	public String toString() {
		return "GuiPact(" + getPact().getId() + ")";
	}
}