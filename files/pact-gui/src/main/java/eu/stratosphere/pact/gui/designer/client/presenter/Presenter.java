package eu.stratosphere.pact.gui.designer.client.presenter;

import com.google.gwt.user.client.ui.HasWidgets;

// interface for presenter
// perfect diagramm: http://www.nieleyde.org/SkywayBlog/post.htm?postid=37782056-c4e1-4dfb-9caa-40ab9552ca3b
public abstract interface Presenter {
	public abstract void go(final HasWidgets container);
}
