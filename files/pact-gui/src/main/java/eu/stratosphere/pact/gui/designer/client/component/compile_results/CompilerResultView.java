package eu.stratosphere.pact.gui.designer.client.component.compile_results;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.IconProvider;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.tree.Tree;

import eu.stratosphere.pact.gui.designer.client.component.compile_results.CompilerResult.CompilationResultTreeNode;
import eu.stratosphere.pact.gui.designer.client.component.compile_results.CompilerResult.CompilerResultViewInterface;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;

/**
 * This component is used in a tabPanel to display the compilation results given
 * as hashMap of files (key=path, value=content)
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class CompilerResultView extends Composite implements CompilerResultViewInterface {
	private CompilerResultView self = this;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Reference to presenter - to access Model
	 */
	private CompilerResult compilerResult = null;

	/**
	 * UiBinder and Interface
	 */
	private static CompilerResultViewUiBinder uiBinder = GWT.create(CompilerResultViewUiBinder.class);

	interface CompilerResultViewUiBinder extends UiBinder<Widget, CompilerResultView> {
	}

	/**
	 * Automatically assign attributes from xml declaration
	 */
	@UiField
	BorderLayoutContainer borderLayoutContainer;

	/**
	 * Container for tree on the left
	 */
	@UiField
	ContentPanel westPanel;

	/**
	 * Center container
	 */
	@UiField
	ContentPanel centerPanel;

	/**
	 * HTML Container for the actual file content
	 */
	@UiField
	HTML centerPanelBody;

	/**
	 * the whole tree Widget on the left
	 */
	Tree<CompilationResultTreeNode, String> filesTreeWidget = null;

	/**
	 * Construct
	 * 
	 * @param appInjector
	 * @param files
	 */
	@Inject
	public CompilerResultView(AppInjector appInjector) {
		super();
		Log.debug(this + ": Constructor");

		this.appInjector = appInjector;
		this.initWidget(uiBinder.createAndBindUi(this));
	}

	/**
	 * Views are injected via the dependency Injection Mechanism GIN and are
	 * mocked for testing with Mockito. Because both technics do not allow to
	 * pass additional parameters to the constructor, this method is usually
	 * called after constructing the view to pass additional data.
	 */
	@Override
	public void prepareWidget(CompilerResult compilerResult) {
		this.compilerResult = compilerResult;

		// create treeWidget and add to west panel
		filesTreeWidget = getTree();
		if (filesTreeWidget != null) {
			westPanel.add(filesTreeWidget);
		}

		// overflow setting is not working via ui-binder, so set it now
		centerPanelBody.getElement().getStyle().setOverflow(Overflow.AUTO);
	}

	/**
	 * Attach handler to download button on the bottom of the center panel Used
	 * to download the selected file
	 * 
	 * @param event
	 */
	@UiHandler({ "downloadButton" })
	public void onButtonClick(SelectEvent event) {
		Log.debug(self + ": Click on download-button");
		if (compilerResult != null &&  compilerResult.getResult() != null && compilerResult.getResult().isDownloadPossible()) {
			Log.info("ServerTempDirPath: " + compilerResult.getResult().getServerTempDirPath());
			String downloadPath = GWT.getModuleBaseURL() + this.appInjector.i18n().dataServletUrl() + "?file="
					+ compilerResult.getResult().getServerTempDirPath()
					+ filesTreeWidget.getSelectionModel().getSelectedItem().getId();
			Log.debug(self + ": File requested for download: " + downloadPath);
			Window.open(downloadPath, "Download", "");
		} else {
			Info.display("No download", "This resource can't be downloaded");
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// all the quite difficult tree stuff starts here
	// /////////////////////////////////////////////////////////////////////////////////////
	private Tree<CompilationResultTreeNode, String> getTree() {
		Log.debug(this + ": create tree widget");
		Tree<CompilationResultTreeNode, String> filesTreeWidget = null;

		if (compilerResult == null || compilerResult.getResult() == null) {
			Info.display("No data received", "No compilation result data received from server");
			Log.info("FilesTreeWidget for compiler results can't be created. compilerResult=" + compilerResult
					+ ", compilerResult.getResult()=null");
		} else {
			// this object provides the label of the tree nodes
			ValueProvider<CompilationResultTreeNode, String> vp = new ValueProvider<CompilationResultTreeNode, String>() {
				@Override
				public String getValue(CompilationResultTreeNode node) {
					return node.getLabel();
				}

				@Override
				public void setValue(CompilationResultTreeNode key, String value) {
				}

				@Override
				public String getPath() {
					return "";
				}
			};

			// create filesTreeWidget
			filesTreeWidget = new Tree<CompilationResultTreeNode, String>(compilerResult.getStore(), vp);

			// Icon Provider for tree
			filesTreeWidget.setIconProvider(new IconProvider<CompilationResultTreeNode>() {
				@Override
				public ImageResource getIcon(CompilationResultTreeNode node) {
					if (node.getChildren().size() > 0) {
						return appInjector.getImages().folder16();
					} else if (node.getLabel().contains(".jar")) {
						return appInjector.getImages().jar16();
					} else if (node.getLabel().contains(".java")) {
						return appInjector.getImages().java16();
					} else if (node.getLabel().contains(".log")) {
						if (compilerResult.getResult().wasSuccessFul()) {
							return appInjector.getImages().log_ok16();
						} else {
							return appInjector.getImages().log_error16();
						}
					} else {
						return appInjector.getImages().file16();
					}
				}
			});

			// expand full tree on start
			filesTreeWidget.setAutoExpand(true);

			// click event on nodes - show file content on the right
			filesTreeWidget.getSelectionModel().addSelectionHandler(new SelectionHandler<CompilationResultTreeNode>() {
				@Override
				public void onSelection(SelectionEvent<CompilationResultTreeNode> event) {
					Log.debug(self + ": Node select - Path='" + event.getSelectedItem().getId() + "'");

					String content = event.getSelectedItem().getContent();
					if (content == null) {
						Log.debug(self + ": No content available");
						// when no content available (eg with jar files)
						content = "<i>no content available. Please download.</i>";
					} else {
						Log.debug(self + ": File content found");
						// otherwise format text content
						content = content.replaceAll("\\r\\n", "\n");
						content = content.replaceAll("\\n", "<br>");
						content = content.replaceAll("\\t", "&#160;&#160;&#160;&#160;");

						// special format for log files
						if (event.getSelectedItem().getId().endsWith(".log")) {
							content = content.replaceAll("(\\d+ [ERROR|INFO])", "####$1") + "####";
							content = content
									.replaceAll("(\\d+ [ERROR]+\\:) (.*?)####",
											"<div style='border-bottom: 1px solid #99BBE8; color: white; background-color: red; font-weight: bold; '>$1 $2\n</div>");
							content = content
									.replaceAll("(\\d+ [INFO]+\\:) (.*?)####",
											"<div style='border-bottom: 1px solid #99BBE8;'><span style='background-color: #ccd9e8'>$1</span> $2</div>\n");
							content = content.replaceAll("####", "");
						}
					}

					// add file content to center panel
					centerPanelBody.setHTML(content);

					// update center panel headline with file name
					((ContentPanel) borderLayoutContainer.getCenterWidget()).setHeadingText(event.getSelectedItem()
							.getLabel());
				}
			});
		}

		return filesTreeWidget;
	}

	/**
	 * select node in tree
	 */
	@Override
	public void selectNode(CompilationResultTreeNode nodeToSelect) {
		if (filesTreeWidget != null) {
			filesTreeWidget.getSelectionModel().select(nodeToSelect, true);
		}
	}

	/**
	 * Method scroll view to bottom - useful to see errors in case the compile
	 * log is the start node
	 * http://stackoverflow.com/questions/4616986/ext-gwt-textarea
	 * -not-scroll-to-bottom
	 */
	@Override
	public void scrollContentViewToBottom() {
		centerPanelBody.getElement().setScrollTop(centerPanelBody.getElement().getScrollHeight());
	}

	public String toString() {
		return "CompilerResultView";
	}
}
