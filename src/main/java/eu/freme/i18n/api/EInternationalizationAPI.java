package eu.freme.i18n.api;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import net.sf.okapi.common.MimeTypeMapper;
import eu.freme.i18n.okapi.nif.converter.ConversionException;
import eu.freme.i18n.okapi.nif.converter.NifConverter;

public class EInternationalizationAPI {

	public static final String MIME_TYPE_XLIFF_1_2 = MimeTypeMapper.XLIFF_MIME_TYPE;

	public static final String MIME_TYPE_HTML = MimeTypeMapper.HTML_MIME_TYPE;
	
	private static final String FREME_NIF_URI_PREFIX = "http://freme-project.eu/";

	private NifConverter converter;

	public EInternationalizationAPI() {

		converter = new NifConverter();
	}

	public Reader convertToTurtle(InputStream is, String mimeType)
			throws ConversionException {

		Reader reader = null;
		InputStream turtleStream = converter.convert2Nif(is, mimeType, FREME_NIF_URI_PREFIX);
		try {
			reader = new InputStreamReader(turtleStream, "UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			//UTF-8 encoding should always be supported
		}
		return reader;
	}
}
