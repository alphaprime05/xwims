package exception;

public class DAOException extends Exception {

	private static final long serialVersionUID = 1L;
	private String errorCode;
	
	public DAOException(String errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
}
