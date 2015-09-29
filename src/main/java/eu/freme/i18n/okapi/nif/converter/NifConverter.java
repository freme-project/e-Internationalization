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
package eu.freme.i18n.okapi.nif.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.pipeline.PipelineReturnValue;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import eu.freme.i18n.okapi.nif.filter.RDFConstants.RDFSerialization;
import eu.freme.i18n.okapi.nif.step.NifParameters;
import eu.freme.i18n.okapi.nif.step.NifWriterStep;

/**
 * This class provides methods for converting a XLIFF and HTML files to NIF
 * format.
 */
public class NifConverter {

	/**
	 * Converts a file to NIF format.
	 * 
	 * @param rawDocument
	 *            the document to be converted
	 * @param mimeType
	 *            the document MIME type
	 * @param sourceLocale
	 *            the source locale
	 * @param uriPrefix
	 *            the URI prefix to be used for resources in NIF document
	 * @return the NIF input stream
	 * @throws ConversionException
	 *             exception raised when an error occurs while converting a
	 *             document to NIF.
	 */
	private InputStream convert2Nif(final InputStream rawDocument,
			String mimeType, final LocaleId sourceLocale,
			final String nifUriPrefix) throws ConversionException {

		InputStream nifInStream = null;
		/*
		 * The Okapi step saves the NIF file into the file system. This is the
		 * file where it should be saved. The user home folder is chosen in
		 * order to be sure that the application has write access.
		 */
		File outputFile = new File(System.getProperty("user.home"),
				"nifConvertedFile" + System.currentTimeMillis());
		ConversionException exception = null;
		try {
			// creates a raw document object from the input stream
			RawDocument document = createRawDocument(rawDocument, mimeType,
					sourceLocale);
			/*
			 * Create the Okapi pipeline. It includes following steps: -
			 * RawDocumentToFilterEventsStep: read a raw document by using the
			 * appropriate filter depending on the MIME Type. Then sends filter
			 * events. - NifWriterStep: handles the events from the filter and
			 * creates a NIF document.
			 */
			XPipeline pipeline = new XPipeline(
					"Raw document to NIF conversion", new XBatch(
							new XBatchItem(document)),
					new RawDocumentToFilterEventsStep(), new XPipelineStep(
							new NifWriterStep(), new XParameter(
									NifParameters.OUTPUT_URI, outputFile
											.toURI().toString()),
							new XParameter(NifParameters.NIF_LANGUAGE,
									RDFSerialization.TURTLE.toRDFLang()),
							new XParameter(NifParameters.NIF_URI_PREFIX,
									nifUriPrefix)));

			// execute the pipeline
			PipelineReturnValue retValue = pipeline.execute();
			if (retValue.equals(PipelineReturnValue.SUCCEDED)) {
				// Retrieve the input stream from the output file and then
				// delete it.
				nifInStream = new FileInputStream(outputFile);
				if (outputFile.exists()) {
					outputFile.delete();
				}
			} else {
				exception = new ConversionException(
						"Unexpected pipeline exit status: " + retValue.name());
			}
		} catch (UnsupportedMimeTypeException e) {
			exception = new ConversionException(e.getMessage(), e);
		} catch (Exception e) {
			exception = new ConversionException(
					"Error while coverting the document", e);
		}
		if (exception != null) {
			throw exception;
		}

		return nifInStream;

	}

	/**
	 * Creates the Okapi raw document object.
	 * 
	 * @param inStream
	 *            the document input stream
	 * @param mimeType
	 *            the document MIME type
	 * @param sourceLocale
	 *            the source locale.
	 * @return the Okapi raw document.
	 * @throws UnsupportedMimeTypeException
	 *             the exception raised when an unsupported MIME type is
	 *             requested.
	 */
	private RawDocument createRawDocument(InputStream inStream,
			String mimeType, LocaleId sourceLocale)
			throws UnsupportedMimeTypeException {
		// Creates the RawDocument object.
		RawDocument document = new RawDocument(inStream, "UTF-8", sourceLocale);
		// Sets the appropriate filter depending on the MIME type
		document.setFilterConfigId(getFilterIdFromMimeType(mimeType));

		/*
		 * The code below has been added, because when dealing with XLIFF files,
		 * Okapi needs to have a valid source locale and a valid target locale.
		 * These info are not relevant to FREME user and it is not necessary
		 * force the user to pass this kind of information. Indeed source and
		 * target locales are well defined into XLIFF file itself and the very
		 * locales retrieved from the file are used by NIF step during
		 * conversion. So the code enclosed in following "if" statement simply
		 * avoids Okapi stopping the conversion and complaining for null source
		 * and target locales.
		 */
		if (mimeType.equals(MimeTypeMapper.XLIFF_MIME_TYPE)) {
			if (sourceLocale == null) {
				document.setSourceLocale(getFakeLocaleId());
			}
			document.setTargetLocale(getFakeLocaleId());
		}
		return document;
	}

	/**
	 * Creates a whatever locale to be used in case of XLIFF file.
	 * 
	 * @return a locale (actually an English locale)
	 */
	private LocaleId getFakeLocaleId() {

		return new LocaleId("en");
	}

	/**
	 * Gets the appropriate Okapi filter based on the MIME type string.
	 * 
	 * @param mimeType
	 *            the MIME type string
	 * @return the filter configuration ID
	 * @throws UnsupportedMimeTypeException
	 *             this is exception is raised if an unsupported MIME type is
	 *             passed as parameter.
	 */
	private String getFilterIdFromMimeType(String mimeType)
			throws UnsupportedMimeTypeException {

		String filterId = null;
		switch (mimeType) {
		case MimeTypeMapper.XLIFF_MIME_TYPE:
			filterId = new XLIFFFilter().getConfigurations().get(0).configId;
			break;
		case MimeTypeMapper.HTML_MIME_TYPE:
			filterId = new HtmlFilter().getConfigurations().get(0).configId;
			break;
		default:
			throw new UnsupportedMimeTypeException(mimeType, new String[] {
					MimeTypeMapper.XLIFF_MIME_TYPE,
					MimeTypeMapper.HTML_MIME_TYPE });
		}
		return filterId;

	}

	/**
	 * Converts a file to NIF format.
	 * 
	 * @param rawDocument
	 *            the document to be converted
	 * @param mimeType
	 *            the document MIME type
	 * @param langCode
	 *            the source language as ISO 639 two-characters code.
	 * @param uriPrefix
	 *            the URI prefix to be used for resources in NIF document
	 * @return the NIF input stream
	 * @throws ConversionException
	 *             exception raised when an error occurs while converting a
	 *             document to NIF.
	 */
	public InputStream convert2Nif(final InputStream rawDocument,
			final String mimeType, final String langCode, final String uriPrefix)
			throws ConversionException {

		return convert2Nif(rawDocument, mimeType,
				langCode != null ? new LocaleId(langCode) : null, uriPrefix);
	}

	/**
	 * Converts a file to NIF format.
	 * 
	 * @param rawDocument
	 *            the document to be converted
	 * @param mimeType
	 *            the document MIME type
	 * @return the NIF input stream
	 * @throws ConversionException
	 *             exception raised when an error occurs while converting a
	 *             document to NIF.
	 */
	public InputStream convert2Nif(final InputStream rawDocument,
			final String mimeType) throws ConversionException {

		return convert2Nif(rawDocument, mimeType, (LocaleId) null, null);
	}

	/**
	 * Converts a file to NIF format.
	 * 
	 * @param rawDocument
	 *            the document to be converted
	 * @param mimeType
	 *            the document MIME type
	 * @param uriPrefix
	 *            the URI prefix to be used for resources in NIF document
	 * @return the NIF input stream
	 * @throws ConversionException
	 *             exception raised when an error occurs while converting a
	 *             document to NIF.
	 */
	public InputStream convert2Nif(final InputStream rawDocument,
			final String mimeType, final String uriPrefix)
			throws ConversionException {

		return convert2Nif(rawDocument, mimeType, (LocaleId) null, uriPrefix);
	}
	
	public static void main(String[] args) {

		try {
			InputStream inStream = new FileInputStream(new File(
					"C:\\Users\\Martab\\test1.xlf"));
//			InputStream inStream = new FileInputStream(new File(
//					"C:\\Users\\Martab\\test12.html"));
			NifConverter converter = new NifConverter();
			InputStream nifFileStream = converter.convert2Nif(inStream,
					MimeTypeMapper.XLIFF_MIME_TYPE, "http://freme-project.eu/");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(nifFileStream, "UTF-8"));
			String line = bufferedReader.readLine();
			while (line != null) {
				System.out.println(line);
				line = bufferedReader.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
