package eu.freme.i18n.okapi.nif.step;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.its.html5.HTML5Filter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.freme.i18n.okapi.nif.filter.RDFConstants.RDFSerialization;

public class NifConversionTest {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	private static final LocaleId DEDE = new LocaleId("de", "de");
	private static final LocaleId ITIT = new LocaleId("it", "it");

	@Test
	public void testDoubleNifFiles() throws URISyntaxException,
			MalformedURLException, FileNotFoundException {

//		File baseDir = new File(this.getClass().getResource("/nifConversion")
//				.toURI());
		File baseDir = new File(this.getClass().getResource("/roundtripping")
				.toURI());
		String pathBase = baseDir.getAbsolutePath();
		String fileName = "html-in";
		String fileExt = ".html";
		String src1Path = pathBase /*+ "/src1/"*/;
		File outFile = new File(pathBase, fileName+fileExt + ".ttl");
		File skelOutFile = new File(pathBase, fileName+"-skeleton"+fileExt + ".ttl");
		RawDocument document = new RawDocument(new FileInputStream(new File(src1Path, fileName+fileExt )), "UTF-8", ENUS);
		document.setTargetLocale(ITIT);
		document.setFilterConfigId(new HTML5Filter().getConfigurations().get(0).configId);
		new XPipeline("Test pipeline for NifWriterStep", new XBatch(
				new XBatchItem(document)// ,

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
						NifParameters.OUTPUT_URI, skelOutFile.toURI().toString()),
						new XParameter(NifParameters.NIF_URI_PREFIX,
								"http://freme-project.eu/"),
						// Defines the desired serialization. Allowed values:
						// RDFSerialization.TURTLE.toRDFLang(),
						// RDFSerialization.JSON-LD.toRDFLang()
						// If null, the output files are saved in RDF format.
						new XParameter(NifParameters.NIF_LANGUAGE,
								RDFSerialization.TURTLE.toRDFLang()))
		,
				new XPipelineStep(new NifWriterStep(), new XParameter(
						NifParameters.OUTPUT_URI, outFile.toURI().toString()),
						new XParameter(NifParameters.NIF_URI_PREFIX,
								"http://freme-project.eu/"),
				// Defines the desired serialization. Allowed values:
				// RDFSerialization.TURTLE.toRDFLang(),
				// RDFSerialization.JSON-LD.toRDFLang()
				// If null, the output files are saved in RDF format.
						new XParameter(NifParameters.NIF_LANGUAGE,
								RDFSerialization.TURTLE.toRDFLang()))
		)
				.execute();
	}
	
//	@Test
	public void testMergeModels(){
	
		InputStream skeleton = getClass().getResourceAsStream("/roundtripping/long-html-skeleton.html.ttl");
		InputStream enriched = getClass().getResourceAsStream("/roundtripping/long-html-enriched.ttl");
		Model skeletonModel = ModelFactory.createDefaultModel();
		InputStreamReader reader = new InputStreamReader(skeleton);
		skeletonModel.read(reader, null, "TTL");
		Model enrichedModel = ModelFactory.createDefaultModel();
		reader = new InputStreamReader(enriched);
		enrichedModel.read(reader, null, "TTL");
		skeletonModel.add(enrichedModel);
		File mergedFile = new File(System.getProperty("user.home"),"merged-long-html.ttl");
		try {
			skeletonModel.write(new OutputStreamWriter(new FileOutputStream(mergedFile)), "TTL");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
