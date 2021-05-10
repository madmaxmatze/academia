package eu.stratosphere.pact.gui.designer.client.component.compile_results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;

import eu.stratosphere.pact.gui.designer.client.event.TabWidgetCloseEvent;
import eu.stratosphere.pact.gui.designer.client.event.TabWidgetCloseEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;

/**
 * This component is used in a tabPanel to display the compilation results given
 * as hashMap of files (key=path, value=content)
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class CompilerResult {
	private CompilerResult self = this;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Interface defines needed methods of all views (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public interface CompilerResultViewInterface extends IsWidget {
		void selectNode(CompilationResultTreeNode startNode);

		void scrollContentViewToBottom();

		HandlerRegistration addShowHandler(ShowHandler showHandler);

		void prepareWidget(CompilerResult compilerResult);
	}

	/**
	 * Reference to the view attached to this presenter (MVP)
	 */
	private CompilerResultViewInterface compilerResultView = null;

	public CompilerResultViewInterface getView() {
		return compilerResultView;
	}

	/**
	 * define initially selected node in tree
	 */
	CompilationResultTreeNode startNode = null;

	public CompilationResultTreeNode getStartNode() {
		return startNode;
	}

	/**
	 * Results returned from server
	 */
	private PactProgramCompilerResult result = null;

	public PactProgramCompilerResult getResult() {
		return result;
	}

	/**
	 * the filesTreeWidget is backed by a virtual store
	 */
	private TreeStore<CompilationResultTreeNode> store = new TreeStore<CompilationResultTreeNode>(new KeyProvider());

	public TreeStore<CompilationResultTreeNode> getStore() {
		return store;
	}

	/**
	 * Construct
	 * 
	 * @param appInjector
	 * @param files
	 */
	public CompilerResult(AppInjector appInjector, PactProgramCompilerResult result) {
		Log.debug(this + ": Constructor");

		this.appInjector = appInjector;
		this.result = result;

		compilerResultView = appInjector.getCompilerResultView();
		prepareWidget();

		// bind all events
		bind();
	}

	/**
	 * Prepare Data to pass to the view
	 */
	private void prepareWidget() {
		// first generate model data

		// only start to do anything if files have been created
		if (result != null) {
			if (result.getServerTempDirPath() == null) {
				result.setServerTempDirPath("");
			}

			if (result.getFiles().size() > 0) {
				Log.debug(this + ": " + result.getFiles().size() + " files have been created during compilation");

				for (Entry<String, String> fileEntry : result.getFiles().entrySet()) {
					String path = fileEntry.getKey();
					String content = fileEntry.getValue();

					Log.debug(this + ": " + fileEntry.getKey() + " (length:"
							+ (content == null ? "0" : content.length()) + ")");

					if (path != null && path.startsWith(result.getServerTempDirPath())) {
						path = path.replaceFirst(result.getServerTempDirPath(), "");
						CompilationResultTreeNode node = getTreeNode(store, path, result.getFiles());

						if (result.wasSuccessFul() && path.endsWith(".jar") && !path.contains("/lib/")) {
							startNode = node;
						}
						if (startNode == null && path.endsWith(".log")) {
							startNode = node;
						}
					}
				}

				this.compilerResultView.prepareWidget(this);
			}
		}
	}

	// single tree node in tree
	class CompilationResultTreeNode implements Serializable, TreeStore.TreeNode<CompilationResultTreeNode> {
		private static final long serialVersionUID = 3056853735087056603L;
		private String id;
		private String label;
		private String content;

		protected CompilationResultTreeNode() {

		}

		public CompilationResultTreeNode(String id, String label, String content) {
			this.id = id;
			this.label = label;
			this.content = content;
		}

		public String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		public String getContent() {
			return content;
		}

		@Override
		public CompilationResultTreeNode getData() {
			return this;
		}

		private List<CompilationResultTreeNode> children = (List<CompilationResultTreeNode>) new ArrayList<CompilationResultTreeNode>();

		@Override
		public List<CompilationResultTreeNode> getChildren() {
			return children;
		}

		public void addChild(CompilationResultTreeNode child) {
			getChildren().add(child);
		}
	}

	/**
	 * Create key for the nodes in the store backing the tree
	 * 
	 * @author MathiasNitzsche@gmail.com
	 */
	public class KeyProvider implements ModelKeyProvider<CompilationResultTreeNode> {
		@Override
		public String getKey(CompilationResultTreeNode item) {
			return item.getId().toString();
		}
	}

	/**
	 * recursive node creator - helper
	 * 
	 * @param store
	 * @param rootPath
	 * @param path
	 * @return
	 */
	private CompilationResultTreeNode getTreeNode(TreeStore<CompilationResultTreeNode> store, String path,
			HashMap<String, String> files) {
		Log.debug(self + ": getTreeNode for path='" + path + "'");

		if (store.findModelWithKey(path) != null) {
			return store.findModelWithKey(path);
		} else if (path == null || "".equals(path) || !path.startsWith("/")) {
			return null;
		}

		int pos = path.lastIndexOf("/");
		String folderPart = path.substring(0, pos);
		String filePart = path.substring(pos + 1);

		// create root if no folder path is left
		if ("".equals(folderPart) && !"/".equals(path)) {
			folderPart = "/";
		} else if ("".equals(filePart)) {
			filePart = "/";
		}
		// Log.debug(self + ": folderPart: " + folderPart + " | filePart:" +
		// filePart);

		CompilationResultTreeNode parentFolder = getTreeNode(store, folderPart, files);
		CompilationResultTreeNode currentFolder = new CompilationResultTreeNode(path, filePart, files.get(result
				.getServerTempDirPath() + path));

		if (parentFolder == null) {
			Log.debug(self + ": root tree node '" + currentFolder.getId() + "' created");
			store.add(currentFolder);
		} else {
			Log.debug(self + ": tree node '" + currentFolder.getId() + "' created and connected to parent node '"
					+ parentFolder.getId() + "'");
			parentFolder.addChild(currentFolder);
			store.add(parentFolder, currentFolder);
		}

		return currentFolder;
	}

	/**
	 * Bind event handler to actions
	 */
	private void bind() {
		Log.debug(this + ": bind");

		// after the compilation result tab is shown - select start node
		compilerResultView.addShowHandler(new ShowHandler() {
			@Override
			public void onShow(ShowEvent event) {
				// select startNode
				if (startNode == null) {
					Log.debug(self + ": On show handler - No start node defined");
				} else {
					Log.debug(self + ": On show handler - Select start node");
					// only select start node if tree widget exists
					compilerResultView.selectNode(startNode);

					// scroll to bottom to see errors in case the compile log is
					// the start node
					if (!result.wasSuccessFul()) {
						compilerResultView.scrollContentViewToBottom();
					}

					// never do this again
					startNode = null;
				}
			}
		});

		/**
		 * when a compilation result tab is closed, the event is catched and
		 * then tried to delete the temp folder on the server
		 */
		appInjector.getEventBus().addHandler(TabWidgetCloseEvent.TYPE, new TabWidgetCloseEventHandler() {
			@Override
			public void onClose(TabWidgetCloseEvent event) {
				if (event.getTabWidget() == self.getView()) {
					if (result != null && result.getServerTempDirPath() != null) {
						Log.info("trigger delete of folder: '" + result.getServerTempDirPath() + "'");
						appInjector.getAjaxService().deletePactTempFolder(result.getServerTempDirPath(),
								new AsyncCallback<Boolean>() {
									@Override
									public void onSuccess(Boolean deleted) {
										Log.info("deletePactTempFolder.onSuccess callback: isdeleted: " + deleted);
									}

									public void onFailure(Throwable caught) {
										Log.error(
												"compilePactProgram - onFailure: " + caught.toString()
														+ caught.getMessage(), caught);
									}
								});
					}
				}
			}
		});
	}

	public String toString() {
		return "CompilerResultTab";
	}
}
