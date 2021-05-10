package eu.stratosphere.pact.gui.designer.client.presenter;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ProgressMessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import eu.stratosphere.pact.gui.designer.client.component.programlist.ProgramList;
import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainerManager;
import eu.stratosphere.pact.gui.designer.client.event.CompilationResultReceivedEvent;
import eu.stratosphere.pact.gui.designer.client.event.ComponentPingEvent;
import eu.stratosphere.pact.gui.designer.client.event.ComponentPingEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.GlobalKeyPressEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramSelectEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramSelectEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramAddEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramRemoveEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.presenter.MainPresenter.MainViewInterface.ButtonType;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;

/**
 * Presenter (within the MVP pattern for the #main page)
 * 
 * @author MathiasNitzsche@gmail.com
 * 
 */
public class MainPresenter implements Presenter {
	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * View attached to this presenter (MVP)
	 */
	private final MainViewInterface view;

	/**
	 * ReferenceToActivePactProgram
	 */
	private PactProgram activePactProgram = null;

	/**
	 * Interface defines needed methods of all view (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public interface MainViewInterface extends IsWidget {
		/**
		 * Types of Buttons a view needs to provide for the main presenter
		 */
		static public enum ButtonType {
			PROGRAM_NEW, PROGRAM_OPEN, PROGRAM_SAVE, PROGRAM_COMPILE, PROGRAM_CLOSE, LOGOUT;
		}

		BorderLayoutContainer getLayout();

		void addClickEvent(ButtonType buttonType, ClickHandler clickHandler);

		void setMenuItemEnable(ButtonType programSave, boolean menuItemsShouldBeEnabled);
	}

	/**
	 * Presenter is constructed with dependency injection object and attached
	 * view
	 * 
	 * @param appInjector
	 * @param view
	 */
	public MainPresenter(AppInjector appInjector, MainViewInterface view) {
		this.appInjector = appInjector;
		this.view = view;

		// pact program manager is a global object and therefore needs to be
		// reseted, when newly creating a main presenter
		appInjector.getPactProgramManager().resetPactPrograms();

		bind();
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	public void bind() {
		/*
		 * appInjector.getEventBus().addHandler(PactProgramRemoveEvent.TYPE, new
		 * PactProgramRemoveEventHandler() {
		 * 
		 * @Override public void onRemovePactProgram(PactProgramRemoveEvent
		 * event) { refreshMenu(); } });
		 * appInjector.getEventBus().addHandler(PactProgramAddEvent.TYPE, new
		 * PactProgramAddEventHandler() {
		 * 
		 * @Override public void onAddPactProgram(PactProgramAddEvent event) {
		 * refreshMenu(); } });
		 */

		/*
		 * Just for developing purpose to check if presenter is garbage
		 * collected after removing from view port for more information on this
		 * - see AppController: customEventBus.removeHandlers();
		 */
		appInjector.getEventBus().addHandler(ComponentPingEvent.TYPE, new ComponentPingEventHandler() {
			@Override
			public void onPing(ComponentPingEvent event) {
				Log.info("Ping: MainPresenter");
			}
		});

		// update menu items
		appInjector.getEventBus().addHandler(PactProgramAddEvent.TYPE, new PactProgramAddEventHandler() {
			@Override
			public void onAddPactProgram(PactProgramAddEvent event) {
				action_RefreshMenu();
			}
		});

		// update menu items
		appInjector.getEventBus().addHandler(PactProgramRemoveEvent.TYPE, new PactProgramRemoveEventHandler() {
			@Override
			public void onRemovePactProgram(PactProgramRemoveEvent event) {
				action_RefreshMenu();
			}
		});

		/*
		 * For the menu item - it important to keep a reference to the current
		 * selected PactProgram
		 */
		appInjector.getEventBus().addHandler(PactProgramSelectEvent.TYPE, new PactProgramSelectEventHandler() {
			@Override
			public void onSelectPactProgram(PactProgramSelectEvent event) {
				activePactProgram = event.getPactProgram();
			}
		});

		/*
		 * Bind handler when clicking on menu item PROGRAM_NEW
		 * 
		 * Asks for pact program name, creates the program in the model and
		 * fires PactProgramAddEvent
		 */
		view.addClickEvent(MainViewInterface.ButtonType.PROGRAM_NEW, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PromptMessageBox box = new PromptMessageBox("Create a new Pact Program",
						"Please enter the name<br>(only letters, digits and underscores):");

				/*
				 * ------------------------------------------- for some reason
				 * all this is buggy:
				 * -------------------------------------------
				 * 
				 * box.getField().addDomHandler(new KeyUpHandler() {
				 * 
				 * @Override public void onKeyUp(KeyUpEvent event) { if
				 * (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				 * // box.hide(); } } }, KeyUpEvent.getType());
				 * 
				 * // box.getField().setAutoValidate(true); //
				 * box.getField().setValidationDelay(500);
				 * 
				 * box.getField().addValidator(new Validator<String>() {
				 * 
				 * @Override public List<EditorError> validate(Editor<String>
				 * editor, String value) { ValidationError nameError =
				 * appInjector
				 * .getPactProgramManager().valdiatePactProgramName(value);
				 * 
				 * Log.info(box.getField().getElement().getFirstChildElement().
				 * getAttribute("value"));
				 * 
				 * ArrayList<EditorError> editorErrors = new
				 * ArrayList<EditorError>(); if (nameError != null) {
				 * EditorError error = new DefaultEditorError(editor,
				 * nameError.getMessage(), value); editorErrors.add(error); }
				 * 
				 * Log.info("validate '" + box.getField().getValue() + "' - '" +
				 * value + "': " + editorErrors.size());
				 * 
				 * return editorErrors; } });
				 */

				box.setResizable(false);
				box.setDraggable(false);
				box.setWidth(300);

				box.addHideHandler(new HideHandler() {
					@Override
					public void onHide(HideEvent event) {
						Dialog dialog = (Dialog) event.getSource();
						String progName = box.getValue();

						if (dialog.getHideButton().getText().toUpperCase() == PredefinedButton.OK.toString()) {
							try {
								action_AddPactProgram(progName);
								Info.display("Success!", "New Pact Program '" + progName + "' created");
							} catch (PactValidationException ex) {
								AlertMessageBox alert = new AlertMessageBox("Failure when creating pact program", ex
										.getMessage());
								alert.setModal(true);
								alert.show();
							}
						} else {
							Info.display("Operation canceled", "No new pact program created");
						}
					}
				});
				box.show();
			}
		});

		/*
		 * Bind handler when clicking on menu item PROGRAM_OPEN
		 * 
		 * Create file upload form and show in popup window. When file upload is
		 * complete, download object via ajax.
		 */
		view.addClickEvent(MainViewInterface.ButtonType.PROGRAM_OPEN, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Create file upload form and show in popup window.
				final com.sencha.gxt.widget.core.client.Window popUp = new com.sencha.gxt.widget.core.client.Window();
				popUp.setModal(true);
				popUp.setHeadingHtml(appInjector.i18n().mainOpenxmlHeadline());
				popUp.setShadow(true);
				popUp.setClosable(true);

				final FormPanel form = new FormPanel();
				form.setAction(GWT.getModuleBaseURL() + appInjector.i18n().dataServletUrl());
				form.setEncoding(FormPanel.ENCODING_MULTIPART);
				form.setMethod(FormPanel.METHOD_POST);

				VerticalPanel formContent = new VerticalPanel();
				formContent.add(new HTML(appInjector.i18n().mainOpenxmlText()));

				// type of upload
				com.sencha.gxt.widget.core.client.form.TextField uploadType = new com.sencha.gxt.widget.core.client.form.TextField();
				uploadType.setName("uploadType");
				uploadType.setValue("pactProgram");
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
						Info.display(appInjector.i18n().mainOpenxmlInfo(), event.getResults());

						// strip html tags from result
						String result = event.getResults();
						Log.info("event.getResults() pure:" + result);
						result = result.replaceAll("\\<.+?\\>", "");
						Log.info("event.getResults():" + result);

						// parse result to json object
						String programHash = null;
						try {
							JSONObject json = (JSONObject) JSONParser.parseStrict(result);
							Log.info("json:" + json);
							JSONString programHashJson = (JSONString) json.get("programHash");
							if (programHashJson != null) {
								programHash = programHashJson.stringValue();
								Log.info("programHash: '" + programHash + "'");
							}
						} catch (Exception e) {
							Log.info("JOSN Exception:" + e.getMessage());
						}

						/*
						 * TODO: at this point the json object should be checked
						 * if the server understood the uploaded program as a
						 * pact and only then start ajay download
						 */

						/*
						 * Start ajay download of new pact program
						 */
						appInjector.getAjaxService().getUploadedPactProgramFromServer(programHash,
								new AsyncCallback<PactProgram>() {
									/*
									 * When server sends pact program, add to
									 * model manager and fire
									 * PactProgramAddEvent event
									 */
									@Override
									public void onSuccess(PactProgram pactProgram) {
										action_AddExistingPactProgram(pactProgram);
									}

									// on failure, show error message
									public void onFailure(Throwable caught) {
										Log.error("Failed RPC");
										Log.error(caught.toString());
									}
								});

						popUp.hide();
						popUp.removeFromParent();
					}
				});

				form.add(formContent);

				popUp.add(form);
				popUp.center();
				popUp.show();
			}
		});

		/*
		 * Bind handler when clicking on menu item PROGRAM_SAVE
		 * 
		 * First ajax upload current pact program and then start download of xml
		 * file in new window.
		 */
		view.addClickEvent(MainViewInterface.ButtonType.PROGRAM_SAVE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (((MenuItem) event.getSource()).isEnabled()) {
					Log.info("activePactProgram " + activePactProgram);
					if (activePactProgram == null) {
						Info.display("Info", "No program opened");
					} else {
						Log.info("Info", "Send whole pact prgram manager to server to save pact program: "
								+ activePactProgram);
						appInjector.getAjaxService().setPactProgramManagerOnServer(
								String.valueOf(appInjector.getPactProgramManager().hashCode()),
								appInjector.getPactProgramManager(), new AsyncCallback<Boolean>() {
									// When ajax upload was successful, start
									// download via servlet
									@Override
									public void onSuccess(Boolean result) {
										Log.info("setPactProgramManagerOnServer - onSuccess - open window for download");
										Window.open(GWT.getModuleBaseURL() + appInjector.i18n().dataServletUrl()
												+ "?programManager=" + appInjector.getPactProgramManager().hashCode()
												+ "&pactid=" + activePactProgram.getId() + "&type=xml", "Xml Download",
												"");
									}

									// on failure, show error message
									public void onFailure(Throwable caught) {
										Log.error(caught.toString());
										Info.display("Info", "Error when saving file");
									}
								});

					}
				}

			}
		});

		/*
		 * Bind handler when clicking on menu item PROGRAM_COMPILE
		 * 
		 * Like save operation. First ajax upload current pact program and then
		 * start download of jar file in new window.
		 */
		view.addClickEvent(MainViewInterface.ButtonType.PROGRAM_COMPILE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (((MenuItem) event.getSource()).isEnabled()) {
					if (activePactProgram == null) {
						Info.display("Info", "No program opened");
					} else {
						final ProgressMessageBox compilationProgressBox = new ProgressMessageBox("Progress",
								"Compiling the pact program, please wait...");
						compilationProgressBox.setProgressText("Compile...");
						compilationProgressBox.setPredefinedButtons(PredefinedButton.CANCEL);
						compilationProgressBox.auto();
						compilationProgressBox.show();

						appInjector.getAjaxService().compilePactProgram(activePactProgram,
								new AsyncCallback<PactProgramCompilerResult>() {
									@Override
									public void onSuccess(PactProgramCompilerResult result) {
										Log.info("compilePactProgram.onSuccess callback");

										if (compilationProgressBox.isVisible()) {
											compilationProgressBox.hide();
											appInjector.getEventBus().fireEvent(
													new CompilationResultReceivedEvent(result));
										}
									}

									// on failure, show error message
									public void onFailure(Throwable caught) {
										Log.error(
												"compilePactProgram - onFailure: " + caught.toString()
														+ caught.getMessage(), caught);
										if (compilationProgressBox.isVisible()) {
											compilationProgressBox.hide();
											Info.display("Info", "onFailure" + caught.getMessage());
										}
									}
								});

					}
				}
			}
		});

		/*
		 * Bind handler when clicking on menu item PROGRAM_CLOSE
		 * 
		 * Just fire PactProgramCloseCurrentEvent, listener-widgets will know
		 * what to do
		 */
		view.addClickEvent(MainViewInterface.ButtonType.PROGRAM_CLOSE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (((MenuItem) event.getSource()).isEnabled()) {
					if (activePactProgram == null) {
						Info.display("Info", "No program opened");
					} else {
						Log.info("getPactProgramManager: " + appInjector.getPactProgramManager().toString());
						Log.info("pactProgram: " + activePactProgram);
						action_ClosePactProgram(activePactProgram);
					}
				}
			}
		});

		/*
		 * Bind handler when clicking on menu item LOGOUT
		 * 
		 * Logout via ajax
		 */
		view.addClickEvent(MainViewInterface.ButtonType.LOGOUT, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				appInjector.getAjaxService().logout(new AsyncCallback<Boolean>() {
					/**
					 * Ajax callback from logout server action
					 * 
					 * @param loggedout
					 *            : boolean if logout was successful
					 */
					public void onSuccess(Boolean loggedout) {
						if (loggedout) {
							Cookies.removeCookie("sid");
							Log.info("Logout done");
							History.newItem("login");
						} else {
							Log.warn("Logout not working");
						}
					}

					// on failure, show error message
					public void onFailure(Throwable caught) {
						Log.error("Failed RPC");
						Log.error(caught.toString());
					}
				});
			}
		});
	}

	/**
	 * Initialization is done in constructor. This seperate method actually
	 * attaches the presenter's view to the viewport and add child widgets
	 */
	public void go(final HasWidgets container) {
		container.clear();
		container.add((Widget) view);

		((Widget) container).addDomHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				appInjector.getEventBus().fireEvent(new GlobalKeyPressEvent(event));
			}
		}, KeyDownEvent.getType());

		ProgramList guiPactProgramList = new ProgramList(appInjector);
		TabContainerManager tabContainerManager = new TabContainerManager(appInjector);

		// attach pact program list to the left and pact program container to
		// the right
		BorderLayoutData westLayoutData = new BorderLayoutData(200);
		westLayoutData.setSplit(true);
		view.getLayout().setWestWidget(guiPactProgramList.getView(), westLayoutData);
		view.getLayout().setCenterWidget(tabContainerManager.getView());

		// during development it is nice to start with an example pact program
		// appInjector.getPactProgramManager().addTestPactProgram();
	}

	/**
	 * extra method for better testing
	 * 
	 * @param progName
	 * @return
	 * @throws PactValidationException
	 */
	public PactProgram action_AddPactProgram(String progName) throws PactValidationException {
		PactProgram newPactProgram = appInjector.getPactProgramManager().createNewPactProgram(progName);
		return newPactProgram;
	}

	/**
	 * extra method for better testing
	 * 
	 * @param pactProgram
	 */
	public void action_AddExistingPactProgram(PactProgram pactProgram) {
		appInjector.getPactProgramManager().addNewPactProgram(pactProgram);
	}

	/**
	 * extra method for better testing
	 * 
	 * @param pactProgram
	 */
	public void action_ClosePactProgram(PactProgram pactProgram) {
		appInjector.getPactProgramManager().removePactProgram(pactProgram);
	}

	/**
	 * When no pact program is opened some menu items (like close) have to be
	 * disabled
	 */
	public void action_RefreshMenu() {
		boolean menuItemsShouldBeEnabled = (appInjector.getPactProgramManager().getPactPrograms().size() != 0);

		Log.info("action_RefreshMenu: enable=" + menuItemsShouldBeEnabled);
		view.setMenuItemEnable(ButtonType.PROGRAM_SAVE, menuItemsShouldBeEnabled);
		view.setMenuItemEnable(ButtonType.PROGRAM_COMPILE, menuItemsShouldBeEnabled);
		view.setMenuItemEnable(ButtonType.PROGRAM_CLOSE, menuItemsShouldBeEnabled);
	}

	public String toString() {
		return "MainPresenter";
	}
}
