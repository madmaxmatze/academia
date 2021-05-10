package eu.stratosphere.pact.gui.designer.shared.model.helper;

/**
 * Custom Error for field validation
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class PactValidationException extends Exception {
	private static final long serialVersionUID = 7860019340673479473L;

	public enum ValidationErrorType {
		NOT_DEFINED, NAME_EXISTS, WRONG_CHARS, WRONG_FIRST_CHAR
	}

	private ValidationErrorType type;
	
	public ValidationErrorType getType() {
		return type;
	}
	
	public PactValidationException(ValidationErrorType type) {
		this(type, null);
	}

	public PactValidationException(ValidationErrorType type, String msg) {
		super(msg);
		this.type = type;
	}

	public String getMessage() {
		String msg = super.getMessage();
		if (msg == null || "".equals(msg)) {
			switch (type) {
			case NOT_DEFINED:
				return "No name defined";
			case WRONG_CHARS:
				return "Only allowed chars are letters, digits and underscore";
			case WRONG_FIRST_CHAR:
				return "Name needs to start with upper case letter";
			case NAME_EXISTS:
				return "Name already exists";
			}
			return "Wrong name.";
		} else {
			return msg;
		}
	}

}
