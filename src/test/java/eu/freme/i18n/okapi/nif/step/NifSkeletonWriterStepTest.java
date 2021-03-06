/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
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
package eu.freme.i18n.okapi.nif.step;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Test;

import eu.freme.i18n.okapi.nif.filter.RDFConstants.RDFSerialization;

public class NifSkeletonWriterStepTest {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	private static final LocaleId DEDE = new LocaleId("de", "de");
	private static final LocaleId ITIT = new LocaleId("it", "it");

	@Test
	public void testPackageFormat() throws URISyntaxException,
			MalformedURLException {
		File baseDir = new File(this.getClass().getResource("/nifConversion")
				.toURI());
		String pathBase = baseDir.getAbsolutePath();
		String src1Path = pathBase + "/ITS/HTML/";
		new XPipeline("Test pipeline for NifWriterStep", new XBatch(
				new XBatchItem(new File(src1Path, "text-analysis2.html").toURI()
						.toURL(), "UTF-8", ENUS, ITIT)// ,

				// new XBatchItem(new File(src1Path,
				// "TestPresentation01.odp").toURI()
				// .toURL(), "UTF-8", ENUS, DEDE),
				//
				// new XBatchItem(new File(src1Path,
				// "TestSpreadsheet01.ods").toURI().toURL(),
				// "UTF-8", ENUS, DEDE)
				),

				// mandatory step --> starting from the raw document, it sends
				// appropriate events, then handled by next steps in the
				// pipeline
				new RawDocumentToFilterEventsStep(),

				new XPipelineStep(new NifSkeletonWriterStep(), new XParameter(
						NifParameters.OUTPUT_BASE_PATH, pathBase),
						new XParameter(NifParameters.NIF_URI_PREFIX, "http://freme-project.eu/"), 
				// Defines the desired serialization. Allowed values:
				// RDFSerialization.TURTLE.toRDFLang(),
				// RDFSerialization.JSON-LD.toRDFLang()
				// If null, the output files are saved in RDF format.
						new XParameter(NifParameters.NIF_LANGUAGE,
								RDFSerialization.TURTLE.toRDFLang())))
				.execute();
	}
}
