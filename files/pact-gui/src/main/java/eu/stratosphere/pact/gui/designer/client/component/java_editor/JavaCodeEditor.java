package eu.stratosphere.pact.gui.designer.client.component.java_editor;

import java.util.ArrayList;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;

import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.TabWidgetSelectEvent;
import eu.stratosphere.pact.gui.designer.client.event.TabWidgetSelectEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;

/**
 * Widget represents content of one pact function tab with code editor inside
 */
public class JavaCodeEditor {
	private JavaCodeEditor self = this;

	/**
	 * Interface defines needed methods of all views which can be provided to
	 * this presenter
	 */
	public interface JavaCodeEditorViewInterface extends IsWidget {
		/**
		 * Types of Buttons a view needs to provide for the main presenter
		 */
		static public enum ButtonType {
			UNDO, REDO, REINDENT, INSERT_STUB;
		}

		void refresh();

		void addClickEvent(ButtonType buttonType, ClickHandler clickHandler);

		void redo();

		void undo();

		void reindent();

		void insertAtCursorOrReplaceSelectedText(String replacement);

		void showInfoToUser(String headline, String content);

		void updateDisplayedPactType();

		void addPactNameValidator(Validator<String> validator);

		void addPactNameChangeHandler(ValueChangeHandler<String> valueChangeHandler);

		void replacePactNameInCode();

		void addPactTypeChangeHandler(ValueChangeHandler<PactType> valueChangeHandler);

		void addDegreeOfParallelismChangeHandler(ValueChangeHandler<Integer> valueChangeHandler);

		void prepareWidget(JavaCodeEditor javaCodeEditor);
	}

	/**
	 * reference to passed view
	 */
	JavaCodeEditorViewInterface javaCodeEditorView = null;

	public JavaCodeEditorViewInterface getView() {
		return javaCodeEditorView;
	}

	/**
	 * Pact the tab is connected to
	 */
	private Pact pact;

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 * 
	 * @param pact
	 *            : Pact the tab is connected to
	 */
	public JavaCodeEditor(AppInjector appInjector, final Pact pact) {
		this.appInjector = appInjector;
		this.pact = pact;

		Log.debug(this + ": Constructor");

		javaCodeEditorView = appInjector.getJavaCodeEditorView();
		javaCodeEditorView.prepareWidget(this);

		bind();
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	private void bind() {
		Log.debug(this + ": bind");

		appInjector.getEventBus().addHandler(PactPropertyChangeEvent.TYPE, new PactPropertyChangeEventHandler() {
			@Override
			public void onPactPropertyChange(PactPropertyChangeEvent event) {
				if (pact == event.getPact()) {
					Log.debug("onPactPropertyChange");
					javaCodeEditorView.updateDisplayedPactType();
				}
			}
		});

		appInjector.getEventBus().addHandler(TabWidgetSelectEvent.TYPE, new TabWidgetSelectEventHandler() {
			@Override
			public void onSelect(TabWidgetSelectEvent event) {
				if (event.getTabWidget() == javaCodeEditorView) {
					javaCodeEditorView.refresh();
				}
			}
		});

		javaCodeEditorView.addClickEvent(JavaCodeEditorViewInterface.ButtonType.UNDO, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": Undo Button Click");
				javaCodeEditorView.undo();
			}
		});

		javaCodeEditorView.addClickEvent(JavaCodeEditorViewInterface.ButtonType.REDO, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": Redo Button Click");
				javaCodeEditorView.redo();
			}
		});

		javaCodeEditorView.addClickEvent(JavaCodeEditorViewInterface.ButtonType.REINDENT, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": Reindent Button Click");
				javaCodeEditorView.reindent();
			}
		});

		javaCodeEditorView.addClickEvent(JavaCodeEditorViewInterface.ButtonType.INSERT_STUB, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.debug(self + ": Insert Stub Button Click");

				String replacement = "";
				switch (pact.getType()) {
				case SOURCE:
					replacement = appInjector.i18n().pactSource();
					break;

				case SINK:
					replacement = appInjector.i18n().pactSink();
					break;

				case PACT_MAP:
					replacement = appInjector.i18n().pactMap();
					break;

				case PACT_REDUCE:
					replacement = appInjector.i18n().pactReduce();
					break;

				default:
					break;
				}

				if (replacement != null && !"".equals(replacement)) {
					Log.debug(self + ": replacment has " + replacement.length() + " bytes");
					replacement = pact.replacePactNameInCode(replacement);
					javaCodeEditorView.insertAtCursorOrReplaceSelectedText(replacement);
				} else {
					javaCodeEditorView.showInfoToUser("No stub existing", "Nothing inserted");
				}
			}
		});

		javaCodeEditorView.addPactNameValidator(new Validator<String>() {
			@Override
			public List<EditorError> validate(Editor<String> editor, String value) {
				Log.debug(self + ": Name Text field validate");
				PactValidationException nameError = action_validateNewPactName(value);

				ArrayList<EditorError> editorErrors = new ArrayList<EditorError>();
				if (nameError != null) {
					EditorError error = new DefaultEditorError(editor, nameError.getMessage(), value);
					editorErrors.add(error);
				}
				return editorErrors;
			}
		});

		javaCodeEditorView.addPactNameChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				Log.debug(self + ": New NameField value: " + event.getValue());

				if (((TextField) event.getSource()).isValid()) {
					action_changePactNameWithoutValidation(event.getValue());
					javaCodeEditorView.showInfoToUser("Saved", "New pact name '" + event.getValue() + "' was saved.");
				} else {
					javaCodeEditorView.showInfoToUser("Not Saved", "Invalid Value. Please correct errors.");
				}
			}
		});

		javaCodeEditorView.addPactTypeChangeHandler(new ValueChangeHandler<PactType>() {
			@Override
			public void onValueChange(ValueChangeEvent<PactType> event) {
				action_changePactType(event.getValue());
			}
		});

		javaCodeEditorView.addDegreeOfParallelismChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				Integer degreeOfParallelism = event.getValue();
				if (degreeOfParallelism == null || degreeOfParallelism < 1 || degreeOfParallelism > 100) {
					javaCodeEditorView.showInfoToUser("Value incorrent", "Must be between 1 and 100");
				}

				action_changeDegreeOfParallelism(degreeOfParallelism);
			}
		});

		javaCodeEditorView.refresh();
	}

	/**
	 * method wrapper for testing
	 */
	protected PactValidationException action_validateNewPactName(String value) {
		return pact.getPactProgram().valdiatePactName(value, pact);
	}

	/**
	 * method wrapper for testing
	 */
	protected void action_changeDegreeOfParallelism(Integer degreeOfParallelism) {
		if (degreeOfParallelism == null || degreeOfParallelism < 1 || degreeOfParallelism > 100) {
			degreeOfParallelism = 1;
		}
		pact.setDegreeOfParallelism(degreeOfParallelism);
	}

	/**
	 * method wrapper for testing
	 */
	protected void action_changePactNameWithoutValidation(String newPactName) {
		pact.setName(newPactName);
		javaCodeEditorView.replacePactNameInCode();
	}

	/**
	 * method wrapper for testing
	 */
	protected void action_changePactType(PactType newPactType) {
		if (newPactType != null) {
			Log.debug(this + "action_changePactType to: " + newPactType.getLabel());
			pact.setType(newPactType);
		}
	}

	public Pact getPact() {
		return pact;
	}

	public String toString() {
		return "JavaCodeEditor(pact: " + pact.getId() + ", " + pact.getName() + ")";
	}
}