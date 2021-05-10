package eu.stratosphere.pact.gui.designer.client.component.java_editor;

import java.util.ArrayList;

import javax.inject.Inject;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.event.CollapseEvent;
import com.sencha.gxt.widget.core.client.event.CollapseEvent.CollapseHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import eu.stratosphere.pact.gui.designer.client.component.java_editor.JavaCodeEditor.JavaCodeEditorViewInterface;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.PactOutputType;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;
import eu.stratosphere.pact.gui.widgets.codemirror.client.CodeMirror;

/**
 * Widget represents content of one pact function tab with code editor inside
 */
public class JavaCodeEditorView extends Composite implements JavaCodeEditorViewInterface {
	/**
	 * Reference to presenter - to access Model
	 */
	private JavaCodeEditor javaCodeEditor = null;

	/**
	 * Dependency Injection object
	 */
	@SuppressWarnings("unused")
	private AppInjector appInjector;

	/**
	 * Embedded code editor object
	 */
	private CodeMirror codeMirror;

	/**
	 * UiBinder for MainView
	 */
	private static JavaCodeEditorUiBinder uiBinder = GWT.create(JavaCodeEditorUiBinder.class);

	/**
	 * Interface for UiBinder for MainView
	 */
	interface JavaCodeEditorUiBinder extends UiBinder<Widget, JavaCodeEditorView> {
	}

	/**
	 * Automatically assign attributes for whole layout
	 */
	@UiField
	BorderLayoutContainer tabContentContainer;

	@UiField
	BorderLayoutContainer codeMirrorContainer;

	@UiField
	ToolBar toolBar;

	@UiField
	TextButton button_undo;

	@UiField
	TextButton button_redo;

	@UiField
	TextButton button_reindent;

	@UiField
	TextButton button_stub;

	@UiField
	ContentPanel propertiesContainer;

	@UiField
	TextField nameTextField;

	@UiField
	FieldLabel typeComboBoxContainer;

	@UiField
	FieldLabel degreeOfParallelismComboBoxContainer;

	/**
	 * Get DropDown Label from PactType Object
	 */
	// http://staging.sencha.com:8080/examples-dev/#ExamplePlace:formsexample(uibinder)
	LabelProvider<PactType> comboLabelProvider = new LabelProvider<PactType>() {
		@Override
		public String getLabel(PactType item) {
			return item.getLabel();
		}
	};

	@UiField(provided = true)
	SimpleComboBox<PactType> typeComboBox = new SimpleComboBox<PactType>(comboLabelProvider);

	// http://staging.sencha.com:8080/examples-dev/#ExamplePlace:formsexample(uibinder)
	LabelProvider<Integer> intComboLabelProvider = new LabelProvider<Integer>() {
		@Override
		public String getLabel(Integer item) {
			return String.valueOf(item);
		}
	};
	@UiField(provided = true)
	SimpleComboBox<Integer> degreeOfParallelismComboBox = new SimpleComboBox<Integer>(intComboLabelProvider);

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 * 
	 * @param pact
	 *            : Pact the tab is connected to
	 */
	@Inject
	public JavaCodeEditorView(AppInjector appInjector) {
		this.appInjector = appInjector;
		Log.debug(this + ": Constructor");
		this.initWidget(uiBinder.createAndBindUi(this));
	}

	/**
	 * Views are injected via the dependency Injection Mechanism GIN and are
	 * mocked for testing with Mockito. Because both technics do not allow to
	 * pass additional parameters to the constructor, this method is usually
	 * called after constructing the view to pass additional data.
	 */
	@Override
	public void prepareWidget(JavaCodeEditor javaCodeEditor) {
		Log.debug(this + ": setPact");
		this.javaCodeEditor = javaCodeEditor;

		codeMirror = getCodeMirrorWidget();
		codeMirrorContainer.setWidget(codeMirror);

		getPropertiesPanel(); // , new BorderLayoutData(255));
	}

	/**
	 * This method provided a way to add click event handlers from the presenter
	 * to this view
	 */
	@Override
	public void addClickEvent(ButtonType buttonType, ClickHandler clickHandler) {
		switch (buttonType) {
		case UNDO:
			button_undo.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case REDO:
			button_redo.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case REINDENT:
			button_reindent.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		case INSERT_STUB:
			button_stub.addDomHandler(clickHandler, ClickEvent.getType());
			break;
		default:
			break;
		}
	}

	/**
	 * To assure saving of text field value, in case the text
	 * field is just removed from dom (eg: closing of tab)
	 */
	@Override
	public void onUnload() {
		if (javaCodeEditor.getPact().getJavaCode() != codeMirror.getValue()) {
			javaCodeEditor.getPact().setJavaCode(codeMirror.getValue());
		}
		super.onUnload();
	}
	
	/**
	 * Internal helper function to get left code editor widget
	 * 
	 * @return
	 */
	private CodeMirror getCodeMirrorWidget() {
		Log.debug(this + ": getCodeMirrorWidget");
		
		// create code editor widget and register handler
		JSONObject config = new JSONObject();
		// http://codemirror.net/manual.html
		config.put("lineNumbers", JSONBoolean.getInstance(true));
		config.put("matchBrackets", JSONBoolean.getInstance(true));
		config.put("mode", new JSONString("text/x-java"));
		config.put("theme", new JSONString("neat"));
		config.put("tabmode", new JSONString("indent"));
		config.put("indentUnit", new JSONNumber(4));
		config.put("value", new JSONString(javaCodeEditor.getPact().getJavaCode()));
		// config.put("indentWithTabs", JSONBoolean.getInstance(true));
		// config.put("workTime", new JSONNumber(200));
		// config.put("workDelay", new JSONNumber(3000));
		CodeMirror codeMirror = new CodeMirror(config);
		codeMirror.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String newValue = event.getValue();
				javaCodeEditor.getPact().setJavaCode(newValue);
				Log.debug("Pact '" + javaCodeEditor.getPact().getName() + "' coding tab - save new value (len:"
						+ newValue.length() + ") to model");
			}
		});

		return codeMirror;
	}

	/**
	 * Internal helper function to get right properties widget Is currently just
	 * an image
	 * 
	 * @return
	 */
	private void getPropertiesPanel() {
		Log.debug(this + ": getPropertiesPanel");

		propertiesContainer.getBody().getStyle().setBackgroundColor("#DFE8F6");
		propertiesContainer.getBody().getStyle().setPadding(10, Unit.PT);

		Log.debug(this + ": init nameTextField");
		if (nameTextField != null) {
			nameTextField.setAutoValidate(true);
		}

		if (javaCodeEditor.getPact().getType().isRealInputContract()) {
			Log.debug(this + ": init typeComboBox");

			// add values for typeComboBox
			typeComboBox.add(PactType.getInputContractTypes());

			// workarounds to propagate value change
			// Important:
			// http://www.sencha.com/forum/showthread.php?127056-SimpleComboBox-setValue-destroys-all-other-options
			// typeComboBox.setTriggerAction(TriggerAction.ALL);
			typeComboBox.addCollapseHandler(new CollapseHandler() {
				@Override
				public void onCollapse(CollapseEvent event) {
					@SuppressWarnings("unchecked")
					SimpleComboBox<PactType> b = ((SimpleComboBox<PactType>) event.getSource());
					// b.getText(); + b.getValue().getLabel()
					// pact.setType(typeComboBox.getCurrentValue(), true);
					// Info.display("CollapseHandler", "CollapseHandler: " +
					// b.getCurrentValue());
					typeComboBox.setValue(b.getCurrentValue(), true);
				}
			});
			// ChangeHandler not working
			// http://www.sencha.com/forum/showthread.php?160547-(Beta)-ComboBox-issues&p=689076
			typeComboBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					Info.display("ChangeHandler",
							"WORKING AGAIN - PLEASE SEE eu.stratosphere.pact.gui.designer.client.component.java_editor.JavaCodeEditor");
					Log.error(this
							+ ": ChangeHandler: WORKING AGAIN - PLEASE SEE eu.stratosphere.pact.gui.designer.client.component.java_editor.JavaCodeEditor");
				}
			});

			// add values to degreeOfParallelismComboBox dropdown
			Log.debug(this + ": init degreeOfParallelism ComboBox");
			ArrayList<Integer> degreeOfParallelismValues = new ArrayList<Integer>();
			for (int i = 1; i < 11; i++) {
				degreeOfParallelismValues.add(i);
			}
			degreeOfParallelismComboBox.add(degreeOfParallelismValues);
			degreeOfParallelismComboBox.setValue(javaCodeEditor.getPact().getDegreeOfParallelism(), false);
			
			// workarounds to propagate value change
			// ChangeHandler not working
			// http://www.sencha.com/forum/showthread.php?160547-(Beta)-ComboBox-issues&p=689076
			degreeOfParallelismComboBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					@SuppressWarnings("unchecked")
					SimpleComboBox<Integer> degreeOfParallelismComboBoxTemp = ((SimpleComboBox<Integer>) event
							.getSource());
					Integer degreeOfParallelism = degreeOfParallelismComboBoxTemp.getCurrentValue();
					Log.info("degreeOfParallelismComboBox ChangeHandler - val:" + degreeOfParallelism);
					degreeOfParallelismComboBoxTemp.setValue(degreeOfParallelism, true, true);
				}
			});
			degreeOfParallelismComboBox.addCollapseHandler(new CollapseHandler() {
				@Override
				public void onCollapse(CollapseEvent event) {
					@SuppressWarnings("unchecked")
					SimpleComboBox<Integer> degreeOfParallelismComboBoxTemp = ((SimpleComboBox<Integer>) event
							.getSource());
					Integer degreeOfParallelism = degreeOfParallelismComboBoxTemp.getCurrentValue();
					Log.info("degreeOfParallelismComboBox onCollapse - val:" + degreeOfParallelism);
					degreeOfParallelismComboBoxTemp.setValue(degreeOfParallelism, true, true);
				}
			});
		}

		// SINK never has an output contract
		if (javaCodeEditor.getPact().getType().getNumberOfOutputs() > 0) {
			Log.debug(this + ": init output type checkboxes");

			VerticalPanel vp = new VerticalPanel();
			for (final PactOutputType outputContract : PactOutputType.values()) {
				final CheckBox checkbox = new CheckBox();
				checkbox.setBoxLabel(outputContract.getLabel());
				checkbox.setValue(javaCodeEditor.getPact().getOutputContracts().contains(outputContract));

				checkbox.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						if (checkbox.getValue()) {
							javaCodeEditor.getPact().removeOutputContract(outputContract);
						} else {
							javaCodeEditor.getPact().addOutputContract(outputContract);
						}
					}
				});
				vp.add(checkbox);
			}
			// currently not needed
			// p.add(new FieldLabel(vp, "Output-Contract"));
		}
	}

	/**
	 * After showing the whole widget. Embedded codeMirror need a refresh
	 */
	public void refresh() {
		Log.debug(this + ": refresh");

		if (nameTextField != null) {
			if (nameTextField.getValue() != null
					&& !nameTextField.getValue().equals(javaCodeEditor.getPact().getName())) {
				nameTextField.setValue(javaCodeEditor.getPact().getName());
			}
		}

		if (typeComboBoxContainer != null) {
			typeComboBoxContainer.setVisible(javaCodeEditor.getPact().getType().isRealInputContract());
		}
		if (typeComboBox != null) {
			if (typeComboBox.getValue() != null && typeComboBox.getValue() != javaCodeEditor.getPact().getType()) {
				typeComboBox.setValue(javaCodeEditor.getPact().getType(), true, true);
			}
		}

		if (degreeOfParallelismComboBoxContainer != null) {
			degreeOfParallelismComboBoxContainer.setVisible(javaCodeEditor.getPact().getType().isRealInputContract());
		}
		if (degreeOfParallelismComboBox != null) {
			if (degreeOfParallelismComboBox.getValue() != null
					&& degreeOfParallelismComboBox.getValue() != javaCodeEditor.getPact().getDegreeOfParallelism()) {
				degreeOfParallelismComboBox.setValue(javaCodeEditor.getPact().getDegreeOfParallelism(), true, true);
			}
		}

		if (codeMirror != null) {
			codeMirror.refreshJSWrapper();
		}

		nameTextField.setValue(javaCodeEditor.getPact().getName());
		typeComboBox.setValue(javaCodeEditor.getPact().getType(), true, true);

		if (button_stub != null) {
			button_stub.setHTML("Insert " + javaCodeEditor.getPact().getType().getLabel() + "Stub");
			button_stub.redraw();
			toolBar.forceLayout();
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void redo() {
		if (codeMirror == null) {
			Log.error("No codeMirror object existing");
		} else {
			codeMirror.redoJSWrapper();
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void undo() {
		if (codeMirror == null) {
			Log.error("No codeMirror object existing");
		} else {
			codeMirror.undoJSWrapper();
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void reindent() {
		if (codeMirror == null) {
			Log.error("No codeMirror object existing");
		} else {
			codeMirror.reindent();
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void insertAtCursorOrReplaceSelectedText(String replacement) {
		try {
			codeMirror.replaceSelection(replacement);
			codeMirror.reindent();
		} catch (Exception e) {
			Log.error("Strange insert stub error: " + e.getMessage());
			Log.error("Cause: ", e.getCause());
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void showInfoToUser(String headline, String content) {
		Info.display("No stub existing", "Nothing inserted");
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void updateDisplayedPactType() {
		if (typeComboBox != null) {
			Log.debug("typeComboBox value='" + typeComboBox.getValue() + "'");
			Log.debug("pactType='" + javaCodeEditor.getPact() + "'");
			if (typeComboBox.getValue() != javaCodeEditor.getPact().getType()) {
				Log.debug("update typeComboBox value");
				typeComboBox.setValue(javaCodeEditor.getPact().getType(), false, true);
			}
			refresh();
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void addPactNameValidator(Validator<String> validator) {
		if (validator == null) {
			Log.error("Given Validator Object is null");
		} else {
			if (nameTextField == null) {
				Log.error("No nameTextField object existing");
			} else {
				nameTextField.addValidator(validator);
			}
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void addPactNameChangeHandler(ValueChangeHandler<String> valueChangeHandler) {
		if (valueChangeHandler == null) {
			Log.error("Given valueChangeHandler-Object is null");
		} else {
			if (nameTextField == null) {
				Log.error("No nameTextField object existing");
			} else {
				nameTextField.addValueChangeHandler(valueChangeHandler);
			}
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void replacePactNameInCode() {
		if (codeMirror == null) {
			Log.error("No codeMirror object existing");
		} else {
			codeMirror.setValue(javaCodeEditor.getPact().replacePactNameInCode(codeMirror.getValue()), true);
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void addPactTypeChangeHandler(ValueChangeHandler<PactType> valueChangeHandler) {
		if (typeComboBox == null) {
			// not every codeEditor has a type combo box
			Log.info("No PactTypeChangeHandler added, because no typeComboBox existing");
		} else {
			typeComboBox.addValueChangeHandler(valueChangeHandler);
		}
	}

	/**
	 * provides view function to presenter
	 */
	@Override
	public void addDegreeOfParallelismChangeHandler(ValueChangeHandler<Integer> valueChangeHandler) {
		if (degreeOfParallelismComboBox == null) {
			// not every codeEditor has a degreeOfParallelismComboBox box
			Log.info("No degreeOfParallelismComboBox added, because no typeComboBox existing");
		} else {
			degreeOfParallelismComboBox.addValueChangeHandler(valueChangeHandler);
		}
	}

	public String toString() {
		return "JavaCodeEditorView for (" + (javaCodeEditor == null ? "PACT not set so far" : javaCodeEditor.getPact())
				+ ")";
	}
}