package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Pact;

/**
 * Event fired over the event bus when pact java tab for user function need to be edit
 */
public class UserFunctionOpenEvent extends GwtEvent<UserFunctionOpenEventHandler> {
	public static final Type<UserFunctionOpenEventHandler> TYPE = new Type<UserFunctionOpenEventHandler>();

	private Pact pact;
	
	public UserFunctionOpenEvent(Pact pact) {
		this.pact = pact;
	}

	@Override
	public Type<UserFunctionOpenEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(UserFunctionOpenEventHandler handler) {
		handler.onUserFunctionOpen(this);
	}

	public Pact getPact() {
		return pact;
	}
	
	@Override
    public String toString() {
      return "UserFunctionOpenEvent";
    }
}