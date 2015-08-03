package eu.freme.i18n.okapi.nif.converter;

import java.util.Arrays;

public class UnsupportedMimeTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UnsupportedMimeTypeException(String unsupportedMimeType, String[] supportedTypes) {
		
		super(buildMessage(unsupportedMimeType, supportedTypes));
	}
	
	private static String buildMessage(String unsupportedMimeType, String[] supportedTypes){
		
		return "Unsupported MIME Type: " + unsupportedMimeType + ". Supported types are " + Arrays.toString(supportedTypes);
	}

}
