package eu.stratosphere.pact.gui.designer.client.gin;

import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * Standard Event Handler doesn't offer removing of all listeners at once, but ResettableEventBus does.
 * This wrapper is needed because ResettableEventBus has no empty public constructor, which is necessary for GIN.
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class CustomEventBus extends ResettableEventBus {
	public CustomEventBus() {
		super(new SimpleEventBus());
	}
}
