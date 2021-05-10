/**
 * Ext GWT 3.0.0-SNAPSHOT - Ext for GWT
 * Copyright(c) 2007-2011, Sencha, Inc.
 * licensing@sencha.com
 *
 * http://sencha.com/license
 */
package eu.stratosphere.pact.gui.designer.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Provides Image Resources for all components
 * 
 * @author MathiasNitzsche@gmail.com
 */
// ibm symbols. http://publib.boulder.ibm.com/infocenter/dmndhelp/v7r0mx/index.jsp?topic=%2Fcom.ibm.wbit.help.comptest.doc%2Ftopics%2Fricons.html
public interface Images extends ClientBundle {
	public Images INSTANCE = GWT.create(Images.class);

	@Source("connection16.png")
	ImageResource connection16();

	@Source("download16.png")
	ImageResource download16();

	@Source("download32.png")
	ImageResource download32();

	@Source("edit16.png")
	ImageResource edit16();

	@Source("file16.png")
	ImageResource file16();

	@Source("folder16.png")
	ImageResource folder16();

	@Source("folder_open16.png")
	ImageResource folderOpen16();

	@Source("graph16.png")
	ImageResource graph16();

	@Source("indent16.png")
	ImageResource indent16();

	@Source("java16.png")
	ImageResource java16();

	@Source("jar16.png")
	ImageResource jar16();
	
	@Source("jar_delete16.png")
	ImageResource jar_delete16();

	@Source("log_error16.png")
	ImageResource log_error16();

	@Source("log_ok16.png")
	ImageResource log_ok16();

	@Source("log_progress16.png")
	ImageResource log_progress16();

	@Source("log_warn16.png")
	ImageResource log_warn16();

	@Source("pact16.png")
	ImageResource pact16();

	@Source("redo16.png")
	ImageResource redo16();

	@Source("remove16.png")
	ImageResource remove16();

	@Source("stub16.png")
	ImageResource stub16();

	@Source("undo16.png")
	ImageResource undo16();

	@Source("upload16.png")
	ImageResource upload16();
}