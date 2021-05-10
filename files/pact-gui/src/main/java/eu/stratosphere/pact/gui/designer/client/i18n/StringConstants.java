package eu.stratosphere.pact.gui.designer.client.i18n;

import com.google.gwt.i18n.client.Constants;

/**
 * Define available Strings in language specific property files within the same
 * directory.
 * 
 * @author MathiasNitzsche@gmail.com
 */
public interface StringConstants extends Constants {
	String helloWorld();

	String goodbyeWorld();

	String exceptionText();

	String dataServletUrl();

	String mainOpenxmlHeadline();

	String mainOpenxmlText();

	String mainOpenxmlButton();

	String mainOpenxmlInfo();

	// @Key("validation.resultbox.headline")
	String validationResultboxHeadline();
	String validationResultboxSuccessMessage();

	String pactSource();

	String pactSink();

	String pactReduce();

	String pactMap();
}
