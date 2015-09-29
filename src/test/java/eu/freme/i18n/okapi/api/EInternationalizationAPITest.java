/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * 					Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * 					Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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
package eu.freme.i18n.okapi.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.freme.i18n.api.EInternationalizationAPI;
import eu.freme.i18n.api.EInternationalizationConfig;
import eu.freme.i18n.okapi.nif.converter.ConversionException;
import eu.freme.i18n.okapi.nif.converter.UnsupportedMimeTypeException;
import eu.freme.i18n.okapi.nif.filter.RDFConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EInternationalizationConfig.class)
public class EInternationalizationAPITest {

	@Autowired
	EInternationalizationAPI eInternationalizationAPI;

	 @Test
	public void testEInternationalizationAPI() {

		InputStream is = getClass().getResourceAsStream(
				"/nifConversion/src1/test1.xlf");
		try {
			Reader nifReader = eInternationalizationAPI.convertToTurtle(is,
					EInternationalizationAPI.MIME_TYPE_XLIFF_1_2);
			Model model = ModelFactory.createDefaultModel();
			model.read(nifReader, null,
					RDFConstants.RDFSerialization.TURTLE.toRDFLang());
			assertFalse(model.isEmpty());
			Reader expectedReader = new InputStreamReader(getClass()
					.getResourceAsStream(
							"/nifConversion/expected_text1.xlf.ttl"), "UTF-8");
			Model expectedModel = ModelFactory.createDefaultModel();
			expectedModel.read(expectedReader, null,
					RDFConstants.RDFSerialization.TURTLE.toRDFLang());
			assertTrue(model.isIsomorphicWith(expectedModel));
		} catch (ConversionException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testEInternationalizationAPIUnsupportedMimeType() {

		String unsupportedMimeType = "unsupp/mime-type";
		InputStream is = getClass().getResourceAsStream(
				"/nifConversion/src1/test1.xlf");
		ConversionException exception = null;
		try {
			eInternationalizationAPI.convertToTurtle(is, unsupportedMimeType);
		} catch (ConversionException e) {
			exception = e;
		}
		Assert.assertNotNull(exception);
		UnsupportedMimeTypeException unsuppException = new UnsupportedMimeTypeException(
				unsupportedMimeType, new String[] {
						EInternationalizationAPI.MIME_TYPE_XLIFF_1_2,
						EInternationalizationAPI.MIME_TYPE_HTML });
		Assert.assertEquals(
				unsuppException.getMessage(),
				exception.getMessage());
		Assert.assertTrue(exception.getCause() instanceof UnsupportedMimeTypeException);
	}

}
