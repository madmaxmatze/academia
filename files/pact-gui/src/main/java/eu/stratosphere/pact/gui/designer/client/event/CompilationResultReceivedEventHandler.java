package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler Interface for related Event
 */
public interface CompilationResultReceivedEventHandler extends EventHandler {
	void onReceive(CompilationResultReceivedEvent event);

}