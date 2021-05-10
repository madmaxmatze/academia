package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;

/**
 * Event fired over the event bus when new file list as compilation results need to be displayed
 */
public class CompilationResultReceivedEvent extends GwtEvent<CompilationResultReceivedEventHandler> {
	public static final Type<CompilationResultReceivedEventHandler> TYPE = new Type<CompilationResultReceivedEventHandler>();
	private PactProgramCompilerResult result;
	
	public CompilationResultReceivedEvent(PactProgramCompilerResult result) {
		this.result = result;
	}

	@Override
	public Type<CompilationResultReceivedEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CompilationResultReceivedEventHandler handler) {
		handler.onReceive(this);
	}

	public PactProgramCompilerResult getResult() {
		return result;
	}
	
	@Override
    public String toString() {
      return "CompilationResultReceivedEvent";
    }
}