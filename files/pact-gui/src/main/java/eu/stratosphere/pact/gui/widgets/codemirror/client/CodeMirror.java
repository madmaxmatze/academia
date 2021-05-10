/**
 * 	@author Dominik Guzei, Mathias Nitzsche
 */
package eu.stratosphere.pact.gui.widgets.codemirror.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.HasInitializeHandlers;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * CodeMirrorModul: A GWT wrapper class around the open source javascript code
 * editor CodeMirror by Marijn Haverbeke (http://codemirror.net)
 * 
 * This version is a GWT 2.3 advancement of Dominik Guzei's GWT CodeMirror
 * https://github.com/DominikGuzei/gwt-codemirror-widget
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class CodeMirror extends Widget implements HasChangeHandlers, HasValue<String>,
		HasInitializeHandlers {
	// the host element that holds our editor instance
	private Element hostElement;

	// a static counter for editor instances
	private static int nextId = 0;

	// the id of this instance
	private String id;

	// a native object reference to the editor
	private JavaScriptObject editor = null;

	private JSONObject config; // the used configuration for this instance

	private boolean valueChangeHandlerInitialized;

	/**
	 * ObjectName for debugging
	 */
	private String debugName = "";

	/**
	 * Sole constructor Calls the real constructor with a standard configuration
	 * object.
	 */
	public CodeMirror() {
		this(new JSONObject());
	}

	/**
	 * Constructor with given configuration object sets up the host element and
	 * html markup used to integrate the editor in the widget framework. Does
	 * not initialize the code mirror editor, this is done in the widget's
	 * onLoad() function.
	 * 
	 * @param config
	 *            - the initial configuration
	 */
	public CodeMirror(JSONObject config) {
		super();

		debugName = "CodeMirror Modul: ";
		// Log.debug(debugName + "Constructor");

		id = "codemirror-editor-" + (++nextId);
		hostElement = DOM.createDiv();
		DOM.setElementProperty(hostElement, "id", id);
		setElement(hostElement);
		this.config = config;
	}

	/**
	 * overrides widget's onLoad function and gets called when the widget is
	 * added to the DOM. This is the time the code mirror editor gets
	 * initialized and can be used. You can register an initialization handler
	 * which gets informed when the editor is scriptable.
	 */
	public void onLoad() {
		// Log.debug(debugName + "onLoad");
		super.onLoad();
		if (editor == null) {
			editor = initEditor(id, config);
		}
	}

	/**
	 * initializes the code mirror instance with given configuration and plugs
	 * it into our host element.
	 * 
	 * @param id
	 *            - the unique instance id
	 * @param conf
	 *            - the code mirror configuration
	 * @return JavaScriptObject editor - the created code mirror instance
	 */
	private native JavaScriptObject initEditor(String id, JSONObject configStr) /*-{
																				// console.log ("initEditor");
																				try {
																				eval('var configObj = ' + configStr);

																				configObj.onCursorActivity = function(editor) {
																				editor.setLineClass(editor.highlightLine, null);
																				editor.highlightLine = editor.setLineClass(editor.getCursor().line,
																				"activeline");
																				};

																				var editor = new $wnd.CodeMirror($doc.getElementById(id), configObj);
																				// console.log (editor);
																				
																				if (editor) {
																				editor.highlightLine = editor.setLineClass(0, "activeline");
																				editor.reindent = function() {
																				var lineCount = this.lineCount();
																				for ( var line = 0; line < lineCount; line++) {
																				this.indentLine(line, "smart");
																				}
																				};
																				
																				editor.reindent();
																				}
																				} catch (ex) {
																				// console.log (ex);
																				}
																				return editor;
																				}-*/;

	public native void refreshJSWrapper() /*-{
											// console.log("focus");

											var editor = this.@eu.stratosphere.pact.gui.widgets.codemirror.client.CodeMirror::editor;
											if (editor) {
											editor.refresh();
											editor.reindent();
											}
											}-*/;

	/**
	 * Get the complete content of this code mirror instance
	 * 
	 * @return content
	 */
	public native String getValueJSWrapper() /*-{
												var editor = this.@eu.stratosphere.pact.gui.widgets.codemirror.client.CodeMirror::editor;
												if (editor) {
												return editor.getValue();
												}
												}-*/;

	/**
	 * Replace the complete content of this editor instance
	 * 
	 * @param content
	 */
	public native void setValueJSWrapper(String content) /*-{
															var editor = this.@eu.stratosphere.pact.gui.widgets.codemirror.client.CodeMirror::editor;
															if (editor) {
															editor.setValue(content);
															}
															}-*/;

	/**
	 * Undo the last action by the user
	 */
	public native void undoJSWrapper() /*-{
										var editor = this.@eu.stratosphere.pact.gui.widgets.codemirror.client.CodeMirror::editor;
										editor.undo();
										}-*/;

	/**
	 * Redo the last undone action by the user
	 */
	public native void redoJSWrapper() /*-{
										var editor = this.@eu.stratosphere.pact.gui.widgets.codemirror.client.CodeMirror::editor;
										editor.redo();
										}-*/;

	/**
	 * Reindent the whole content in the editor
	 */
	public native void reindent() /*-{
									var editor = this.@eu.stratosphere.pact.gui.widgets.codemirror.client.CodeMirror::editor;
									editor.reindent();
									}-*/;

	/**
	 * Replace the current selection with any text
	 * 
	 * @param text
	 */
	public native void replaceSelectionJSWrapper(String text) /*-{
																var editor = this.@eu.stratosphere.pact.gui.widgets.codemirror.client.CodeMirror::editor;
																editor.replaceSelection(text);
																}-*/;

	/**
	 * Callback function for the code mirror instance that gets called when the
	 * editor is loaded and ready. Fires an Initialize Event which indicates
	 * that the editor can be used.
	 */
	private void editorLoaded() {
		// Log.debug(debugName + "editorLoaded");
		InitializeEvent.fire(this);
	}

	/**
	 * Callback function for the code mirror instance that gets called when the
	 * user edits the content Fires an ValueChangeEvent with the new content
	 */
	private void onChange() {
		// Log.debug(debugName + "onChange Event");
		ValueChangeEvent.fire(this, getValueJSWrapper());
	}

	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return addDomHandler(handler, ChangeEvent.getType());
	}

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		if (!valueChangeHandlerInitialized) {
			valueChangeHandlerInitialized = true;
			addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					ValueChangeEvent.fire(CodeMirror.this, getValue());
				}
			});
		}

		return addHandler(handler, ValueChangeEvent.getType());
	}

	public String getValue() {
		String value = getValueJSWrapper();
		if (value == null) {
			value = "";
		}
		return value;
	}

	public void setValue(String value) {
		setValue(value, true);
	}

	public void setValue(String value, boolean fireEvents) {
		// Log.debug(debugName + "setValue");
		String oldValue = getValueJSWrapper();
		if (value == null) {
			// Log.debug(debugName +
			// "setValue - new value changed from null to ''");
			value = "";
		}
		if (!value.equals(oldValue)) {
			setValueJSWrapper(value);
			if (fireEvents) {
				ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
			}
		}
	}

	public void replaceSelection(String value) {
		replaceSelection(value, true);
	}

	public void replaceSelection(String value, boolean fireEvents) {
		// Log.debug(debugName + "replaceSelection");
		String oldValue = getValue();
		if (value == null) {
			value = "";
		}
		replaceSelectionJSWrapper(value);
		String newValue = getValue();
		if (!oldValue.equals(newValue) && fireEvents) {
			ValueChangeEvent.fireIfNotEqual(this, oldValue, newValue);
		}
	}

	public HandlerRegistration addInitializeHandler(InitializeHandler handler) {
		return addHandler(handler, InitializeEvent.getType());
	}
}
