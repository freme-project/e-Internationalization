/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
