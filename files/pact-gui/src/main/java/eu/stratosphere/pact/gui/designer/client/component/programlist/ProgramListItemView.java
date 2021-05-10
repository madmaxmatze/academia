package eu.stratosphere.pact.gui.designer.client.component.programlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.IconProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.AccordionLayoutAppearance;
import com.sencha.gxt.widget.core.client.event.ExpandEvent.ExpandHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;

import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramListItem.ProgramListItemViewInterface;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionAddEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionFocusEvent;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionFocusEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionRemoveEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactAddEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactFocusEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactFocusEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramPropertyChangeEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactRemoveEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.Connection;
import eu.stratosphere.pact.gui.designer.shared.model.JarFile;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;

/**
 * Single Item in PactProgramList Widget (Accordion on the left)
 */
public class ProgramListItemView extends ContentPanel implements ProgramListItemViewInterface {
	private ProgramListItemView self = this;

	public static AccordionLayoutAppearance appearance = GWT
			.<AccordionLayoutAppearance> create(AccordionLayoutAppearance.class);

	/**
	 * Reference to presenter - to access Model
	 */
	private ProgramListItem programListItem;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * This class provides a key for the tree view based on the tree node model
	 * behind it
	 * 
	 * @author MathiasNitzsche@gmail.com
	 */
	class KeyProvider implements ModelKeyProvider<PactProgramObjectTreeNode> {
		@Override
		public String getKey(PactProgramObjectTreeNode item) {
			return item.getId().toString();
		}
	}

	/**
	 * Model behind tree widget
	 */
	private TreeStore<PactProgramObjectTreeNode> store = new TreeStore<PactProgramObjectTreeNode>(new KeyProvider());

	/**
	 * Tree Widget, listing all PactProgramObjects
	 */
	private Tree<PactProgramObjectTreeNode, String> treeWidget = null;

	/**
	 * Add and remove jar buttons in toolbar
	 */
	private TextButton addJarTextButton = new TextButton();

	private TextButton removeJarTextButton = new TextButton();

	/**
	 * Settings button on the top right of accordion item
	 */
	private ToolButton programChangePropertiesToolButton = new ToolButton(ToolButton.GEAR);

	/**
	 * right click context menu of accordion item header
	 */
	private MenuItem headerContextMenuEditItem;

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 *            : Dependency Injection object
	 * @param guiPactProgramList
	 * @param pactProgram
	 *            : Connected pactProgram
	 */
	@Inject
	public ProgramListItemView(final AppInjector appInjector) {
		super(appearance);

		this.appInjector = appInjector;

		Log.info(this + ": Constructor");

		// basic settings
		this.setCollapsible(true);
		this.setExpanded(true);
		this.setTitleCollapse(true);
		this.setAnimCollapse(false);
		this.setCollapsible(true);

		// edit pactPrgram properties
		this.addTool(programChangePropertiesToolButton);
		Menu headerContextMenu = new Menu();
		headerContextMenuEditItem = new MenuItem("Edit PACT-Program Properties");
		headerContextMenu.add(headerContextMenuEditItem);
		this.getHeader().setContextMenu(headerContextMenu);

		// create layout
		VerticalPanel vp = new VerticalPanel();

		// create toolbar with 2 buttons
		ToolBar toolBar = new ToolBar();
		addJarTextButton.setIcon(appInjector.getImages().jar16());
		addJarTextButton.setText("Add Jar");
		addJarTextButton
				.setToolTip("Import an external .jar file which will be embedded into the pact program fat jar");
		toolBar.add(addJarTextButton);

		removeJarTextButton.setIcon(appInjector.getImages().jar_delete16());
		removeJarTextButton.setText("Remove Jar");
		removeJarTextButton.setToolTip("Remove selected Jar");
		removeJarTextButton.disable();
		toolBar.add(removeJarTextButton);
		vp.add(toolBar);

		// create tree
		treeWidget = getTree(store);
		treeWidget.getElement().addClassName("treeWidget");
		treeWidget.setHeight(1000);

		treeWidget.getElement().getStyle().setBackgroundColor("white");
		vp.add(treeWidget);

		// add layout to ProgramListItem Content Panel
		this.add(vp);

		bind();
	}

	/**
	 * Views are injected via the dependency Injection Mechanism GIN and are
	 * mocked for testing with Mockito. Because both technics do not allow to
	 * pass additional parameters to the constructor, this method is usually
	 * called after constructing the view to pass additional data.
	 */
	public void prepareWidget(ProgramListItem programListItem) {
		this.programListItem = programListItem;
		this.setHeadingText(programListItem.getPactProgram().getName() + " (ID:"
				+ programListItem.getPactProgram().getId() + ")");
		updateTree();
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	private void bind() {
		// settings button on the top right in header
		programChangePropertiesToolButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				editPactProgramProperties();
			}
		});

		// edit item in header content menu
		headerContextMenuEditItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				editPactProgramProperties();
			}
		});

		/*
		 * When a connection was removed in Graph, this change need to be
		 * reflected in tree
		 */
		appInjector.getEventBus().addHandler(ConnectionRemoveEvent.TYPE, new ConnectionRemoveEventHandler() {
			@Override
			public void onConnectionRemove(ConnectionRemoveEvent event) {
				if (event.getConnection().getPactProgram() == programListItem.getPactProgram()) {
					Log.debug(self + ": ConnectionRemoveEvent");
					updateTree();
				}
			}
		});

		/*
		 * When a connection was added in Graph, this change need to be
		 * reflected in tree
		 */
		appInjector.getEventBus().addHandler(ConnectionAddEvent.TYPE, new ConnectionAddEventHandler() {
			@Override
			public void onConnectionAdd(ConnectionAddEvent event) {
				if (event.getConnection().getPactProgram() == programListItem.getPactProgram()) {
					Log.debug(self + ": ConnectionAddEvent");
					updateTree();

					selectNodeWithId(event.getConnection().getId());
				}
			}
		});

		/*
		 * When a connection was focuesed in Graph, this change need to be
		 * reflected in tree
		 */
		appInjector.getEventBus().addHandler(ConnectionFocusEvent.TYPE, new ConnectionFocusEventHandler() {
			@Override
			public void onConnectionFocus(ConnectionFocusEvent event) {
				if (event.getConnection().getPactProgram() == programListItem.getPactProgram()) {
					Log.debug(self + ": ConnectionFocusEvent");
					selectNodeWithId(event.getConnection().getId());
				}
			}
		});

		/**
		 * When pact was removed from the model, this change need to be
		 * reflected in tree
		 */
		appInjector.getEventBus().addHandler(PactRemoveEvent.TYPE, new PactRemoveEventHandler() {
			@Override
			public void onPactRemove(PactRemoveEvent event) {
				if (event.getPact().getPactProgram() == programListItem.getPactProgram()) {
					Log.debug(self + ": PactRemoveEvent");
					updateTree();
				}
			}
		});

		/**
		 * When pact was added to the model, this change need to be reflected in
		 * tree
		 */
		appInjector.getEventBus().addHandler(PactAddEvent.TYPE, new PactAddEventHandler() {
			@Override
			public void onPactAdd(PactAddEvent event) {
				if (event.getPact().getPactProgram() == programListItem.getPactProgram()) {
					Log.debug(self + ": PactAddEvent");
					updateTree();

					selectNodeWithId(event.getPact().getId());
				}
			}
		});

		/**
		 * When pact properties (like name) changed, this change need to be
		 * reflected in tree
		 */
		appInjector.getEventBus().addHandler(PactPropertyChangeEvent.TYPE, new PactPropertyChangeEventHandler() {
			@Override
			public void onPactPropertyChange(PactPropertyChangeEvent event) {
				if (event.getPact().getPactProgram() == programListItem.getPactProgram()) {
					Log.debug(self + ": PactPropertyChangeEvent");
					updateTree();
				}
			}
		});

		/**
		 * When pact program properties (like jarFiles, name) changed, this
		 * change need to be reflected in tree
		 */
		appInjector.getEventBus().addHandler(PactProgramPropertyChangeEvent.TYPE,
				new PactProgramPropertyChangeEventHandler() {
					@Override
					public void onPactProgramPropertyChange(PactProgramPropertyChangeEvent event) {
						if (event.getPactProgram() == programListItem.getPactProgram()) {
							updateTree();
						}
					}
				});

		/**
		 * When pact was focused in graph, this change need to be reflected in
		 * tree
		 */
		appInjector.getEventBus().addHandler(PactFocusEvent.TYPE, new PactFocusEventHandler() {
			@Override
			public void onPactFocus(PactFocusEvent event) {
				if (event.getPact().getPactProgram() == programListItem.getPactProgram()) {
					selectNodeWithId(event.getPact().getId());
				}
			}
		});
	}

	/**
	 * Update Tree (delete/add nodes in response to changes of the pact program)
	 */
	private void updateTree() {
		Log.debug(this + ": updateTree");

		// reset store behind tree - now store is empty
		store.clear();

		// dummy tree Node
		PactProgramObjectTreeNode pactProgramObjectTreeNode;

		if (programListItem != null && programListItem.getPactProgram() != null) {
			// add all pacts to tree
			for (Pact pact : programListItem.getPactProgram().getPacts().values()) {
				if (pact.getType() == PactType.JAVA) {
					pactProgramObjectTreeNode = getTreeNode(store, "Java/" + pact.getId());
					pactProgramObjectTreeNode.setPact(pact);
				} else {
					pactProgramObjectTreeNode = getTreeNode(store, "PACT/" + pact.getId());
					pactProgramObjectTreeNode.setPact(pact);
				}
			}

			// add all conntection to tree
			for (Connection connection : programListItem.getPactProgram().getConnections().values()) {
				pactProgramObjectTreeNode = getTreeNode(store, "Connection/" + connection.getId());
				pactProgramObjectTreeNode.setConnection(connection);
			}

			// add all jar files to tree
			for (JarFile jarFile : programListItem.getPactProgram().getJarFiles().values()) {
				pactProgramObjectTreeNode = getTreeNode(store, "Jar/" + jarFile.getId());
				pactProgramObjectTreeNode.setJarFile(jarFile);
			}
		}
		// hide tree if no node
		treeWidget.setVisible(store.getRootCount() > 0);

		// initially expanded
		treeWidget.expandAll();

		// this.expand();
		// this.refresh();
	}

	/**
	 * Shorthand to find tree node for pact/java/connection-id (unique across
	 * pactProgram)
	 */
	public void selectNodeWithId(int id) {
		PactProgramObjectTreeNode PactProgramObjectTreeNode = store.findModelWithKey("Java/" + id);
		if (PactProgramObjectTreeNode == null) {
			PactProgramObjectTreeNode = store.findModelWithKey("PACT/" + id);
		}
		if (PactProgramObjectTreeNode == null) {
			PactProgramObjectTreeNode = store.findModelWithKey("Connection/" + id);
		}

		if (PactProgramObjectTreeNode != null) {
			if (treeWidget.getSelectionModel().getSelectedItem() != PactProgramObjectTreeNode) {
				treeWidget.getSelectionModel().select(PactProgramObjectTreeNode, false);
			}
		}
	}

	/**
	 * Upload jar file dialog
	 */
	public void showJarUploadForm(final JarUploadHandler jarUploadHandler) {
		Log.debug(this + ": upload jar file dialog");

		if (jarUploadHandler != null) {
			// Create file upload form and show in popup window.
			final com.sencha.gxt.widget.core.client.Window popUp = new com.sencha.gxt.widget.core.client.Window();
			popUp.setModal(true);
			popUp.setHeadingHtml("Upload Jar File");
			popUp.setShadow(true);
			popUp.setClosable(true);

			final FormPanel form = new FormPanel();
			form.setAction(GWT.getModuleBaseURL() + appInjector.i18n().dataServletUrl());
			form.setEncoding(FormPanel.ENCODING_MULTIPART);
			form.setMethod(FormPanel.METHOD_POST);

			VerticalPanel formContent = new VerticalPanel();
			formContent.add(new HTML("Please select jar from your hard drive"));

			// type of upload
			com.sencha.gxt.widget.core.client.form.TextField uploadType = new com.sencha.gxt.widget.core.client.form.TextField();
			uploadType.setName("uploadType");
			uploadType.setValue("jar");
			uploadType.setVisible(false);
			formContent.add(uploadType);

			// Create a FileUpload widget.
			final FileUpload fileUpload = new FileUpload();
			fileUpload.setName("fileUpload");
			formContent.add(fileUpload);

			// register click handler to upload button
			Button submitButton = new Button(appInjector.i18n().mainOpenxmlButton(), new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (!"".equals(fileUpload.getFilename()) && fileUpload.getFilename() != null) {
						form.submit();
					}
				}
			});
			formContent.add(submitButton);

			/*
			 * Handler when upload is done. Then start downloading the pact
			 * program via ajax
			 */
			form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
				@Override
				public void onSubmitComplete(SubmitCompleteEvent event) {
					Info.display("Jar upload", "success");
					String result = event.getResults();

					popUp.hide();
					popUp.removeFromParent();

					jarUploadHandler.handle(result);
				}
			});

			form.add(formContent);

			popUp.add(form);
			popUp.center();
			popUp.show();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// all the quite difficult tree stuff starts here
	// /////////////////////////////////////////////////////////////////////////////////////
	private Tree<PactProgramObjectTreeNode, String> getTree(final TreeStore<PactProgramObjectTreeNode> store) {
		Log.debug(this + ": create tree widget");
		// get display value for tree node
		ValueProvider<PactProgramObjectTreeNode, String> vp = new ValueProvider<PactProgramObjectTreeNode, String>() {
			@Override
			public String getValue(PactProgramObjectTreeNode node) {
				return node.getLabel();
			}

			@Override
			public void setValue(PactProgramObjectTreeNode key, String value) {
			}

			@Override
			public String getPath() {
				return "";
			}
		};

		// create tree widget
		final Tree<PactProgramObjectTreeNode, String> tree = new Tree<PactProgramObjectTreeNode, String>(store, vp);

		// create icon provider to set different icons for different types of
		// tree nodes
		tree.setIconProvider(new IconProvider<PactProgramObjectTreeNode>() {
			@Override
			public ImageResource getIcon(PactProgramObjectTreeNode node) {
				Pact pact = node.getPact();
				if (pact != null) {
					if (pact.getType() == PactType.JAVA) {
						return appInjector.getImages().java16();
					} else if (pact.getType() == PactType.SINK) {
						return appInjector.getImages().download16();
					} else if (pact.getType() == PactType.SOURCE) {
						return appInjector.getImages().upload16();
					} else {
						return appInjector.getImages().pact16();
					}
				} else if (node.getConnection() != null) {
					return appInjector.getImages().connection16();
				} else if (node.getJarFile() != null) {
					return appInjector.getImages().jar16();
				}

				return appInjector.getImages().folder16();
			}
		});

		// initially expand
		tree.setAutoExpand(true);

		// on click/select on tree node
		tree.getSelectionModel().addSelectionHandler(new SelectionHandler<PactProgramObjectTreeNode>() {
			@Override
			public void onSelection(SelectionEvent<PactProgramObjectTreeNode> event) {
				if (event.getSelectedItem() != null) {
					event.getSelectedItem().onSelection();
				}
			}
		});

		return tree;
	}

	/**
	 * model for one node in tree
	 */
	class PactProgramObjectTreeNode implements Serializable, TreeStore.TreeNode<PactProgramObjectTreeNode> {
		private static final long serialVersionUID = 3056853735087056603L;
		private String id;

		protected PactProgramObjectTreeNode() {

		}

		private JarFile jarFile = null;

		public void setJarFile(JarFile jarFile) {
			this.jarFile = jarFile;
		}

		public JarFile getJarFile() {
			return jarFile;
		}

		private Pact pact = null;

		public void setPact(Pact pact) {
			this.pact = pact;
		}

		public Pact getPact() {
			return pact;
		}

		private Connection connection = null;

		public void setConnection(Connection connection) {
			this.connection = connection;
		}

		public Connection getConnection() {
			return connection;
		}

		public PactProgramObjectTreeNode(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public String getLabel() {
			if (connection != null) {
				return "" + connection.getId();
			} else if (jarFile != null) {
				return jarFile.getName();
			} else if (pact != null) {
				return (pact.getType().isSinkOrSource() ? pact.getType().getLabel()
						+ programListItem.getPactProgram().getSourceOrSinkNumber(pact) + ": " : "")
						+ pact.getName();
			} else if (getParent() == null) {
				return "/";
			}
			return (id.length() > 1 ? id.replaceFirst("\\/", "") : id);
		}

		public void onSelection() {
			removeJarTextButton.setEnabled(this.getJarFile() != null);
			if (pact != null) {
				appInjector.getEventBus().fireEvent(new PactFocusEvent(pact));
			} else if (connection != null) {
				appInjector.getEventBus().fireEvent(new ConnectionFocusEvent(connection));
			}
		}

		@Override
		public PactProgramObjectTreeNode getData() {
			return this;
		}

		private List<PactProgramObjectTreeNode> children = (List<PactProgramObjectTreeNode>) new ArrayList<PactProgramObjectTreeNode>();

		@Override
		public List<PactProgramObjectTreeNode> getChildren() {
			return children;
		}

		public void addChild(PactProgramObjectTreeNode child) {
			getChildren().add(child);
		}
	}

	/**
	 * recursively fill store (which is the model behind tree view)
	 * 
	 * @param store
	 * @param path
	 * @return
	 */
	private PactProgramObjectTreeNode getTreeNode(TreeStore<PactProgramObjectTreeNode> store, String path) {
		if (store.findModelWithKey(path) != null) {
			return store.findModelWithKey(path);
		}

		PactProgramObjectTreeNode currentNode = new PactProgramObjectTreeNode(path);

		int lastSeperatorIndex = path.lastIndexOf("/");
		if (lastSeperatorIndex > -1) {
			String parentPath = path.substring(0, lastSeperatorIndex);
			PactProgramObjectTreeNode parentNode = getTreeNode(store, parentPath);

			parentNode.addChild(currentNode);
			store.add(parentNode, currentNode);
		} else {
			store.add(currentNode);
		}

		return currentNode;
	}

	// edit name
	public void editPactProgramProperties() {
		Info.display("Edit Pact Program Properties", "So far not implemented");
	}

	@Override
	public void remove() {
		this.removeFromParent();
	}

	@Override
	public void addClickEvent(ButtonType buttonType, ClickHandler clickHandler) {
		switch (buttonType) {
		case JAR_ADD:
			addJarTextButton.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case JAR_REMOVE:
			removeJarTextButton.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		default:
			break;
		}
	}

	/**
	 * using the buttons above the tree, it possible to add or remove jar files
	 * from pact program. This is done by the presenter who needs to know if a
	 * and which jar file is selected
	 */
	@Override
	public JarFile getSelectedJarFile() {
		JarFile selectedJarFile = null;

		PactProgramObjectTreeNode selectedTreeNode = treeWidget.getSelectionModel().getSelectedItem();
		if (selectedTreeNode != null && selectedTreeNode.getJarFile() != null) {
			selectedJarFile = selectedTreeNode.getJarFile();
		}

		return selectedJarFile;
	}

	/**
	 * this method is a little workaround to be able to listen to fire the
	 * program select event, based on the event when an accordion item is
	 * expanded
	 */
	@Override
	public void bindExpandHandler(ExpandHandler expandHandler) {
		this.addExpandHandler(expandHandler);
	}

	public String toString() {
		return "PactProgramListItemView ("
				+ (programListItem == null || programListItem.getPactProgram() == null ? "no pactProgram so far"
						: " (id:" + programListItem.getPactProgram().getId() + ", name:"
								+ programListItem.getPactProgram().getName()) + ")";
	}

}