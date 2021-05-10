package eu.stratosphere.pact.gui.designer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;

// compare JS/YUI and GWT: http://blog.phiz.net/gwt-its-not-you-its-me

// http://staging.sencha.com:8080/examples-dev/#ExamplePlace:overview

// inline editiing: http://thezukunft.com/2010/02/05/an-editable-label-for-gwt-with-uibinder-and-eventhandlers/

// D:\Documents\Docs\.m2\repository\com\google\appengine\appengine-java-sdk\1.5.2\appengine-java-sdk-1.5.2\bin\appcfg --enable_jar_splitting --email="mathiasnitzsche@gmail.com" update "D:\PortableApps\_others\android\eclipse_workspace\gui4gae\target\gui4gae-0.0.1-SNAPSHOT"

// rollback: "D:\PortableApps\_others\android\EclipsePortable\App\Eclipse\plugins\com.google.appengine.eclipse.sdkbundle_1.5.1.r36v201106211634\appengine-java-sdk-1.5.1\bin\appcfg.cmd" rollback war

// More stuff about GWT session: - http://pastebin.com/hCbD0j98 -
// http://www.mail-archive.com/google-web-toolkit@googlegroups.com/msg11822.html

		 
/**
 * Entry point class for the whole application
 */
public class Designer implements EntryPoint {
	/**
	 * Actual entry point - like main()
	 */
	public void onModuleLoad() {
		// Create gin dependency injector
		AppInjector appInjector = GWT.create(AppInjector.class);

		// create app controller which is the main presenter
		AppController appController = new AppController(appInjector);

		// pass container to the app controler where the app should be rendered
		// in
		appController.go(RootLayoutPanel.get());
	}
}