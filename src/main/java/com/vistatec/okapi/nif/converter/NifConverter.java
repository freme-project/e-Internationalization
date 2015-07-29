package com.vistatec.okapi.nif.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.PipelineReturnValue;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import com.vistatec.okapi.nif.filter.RDFConstants.RDFSerialization;
import com.vistatec.okapi.nif.step.NifParameters;
import com.vistatec.okapi.nif.step.NifWriterStep;

public class NifConverter {

	public InputStream convert2Nif(final InputStream rawDocument,
			final LocaleId sourceLocale) {
		InputStream nifInStream = null;
//		File baseDir = new File(System.getProperty("user.home"));
		File outputFile = new File(System.getProperty("user.home"), "nifConvertedFile" + System.currentTimeMillis() );
		XPipeline pipeline = new XPipeline("Conversion raw document to NIF",
				new XBatch(new XBatchItem(new RawDocument(rawDocument, "UTF-8",
						sourceLocale))), new RawDocumentToFilterEventsStep(),
				new XPipelineStep(new NifWriterStep(), new XParameter(
						NifParameters.OUTPUT_URI, outputFile.toURI().toString()), new XParameter(
						NifParameters.NIF_LANGUAGE, RDFSerialization.TURTLE
								.toRDFLang())));
		PipelineReturnValue retValue = pipeline.execute();
		System.out.println(retValue.name());
		if(retValue.equals(PipelineReturnValue.SUCCEDED)){
			try {
				nifInStream = new FileInputStream(outputFile);
				outputFile.delete();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return nifInStream;
		
	}

	public InputStream convert2Nif(final InputStream rawDocument, final String langCode) {

		if (langCode != null) {
			return convert2Nif(rawDocument, new LocaleId(langCode));
		}
		return null;
	}
	
	public static void main(String[] args) {
		
		try {
			InputStream inStream = new FileInputStream(new File("C:\\Users\\Martab\\test1.xlf"));
			NifConverter converter = new NifConverter();
			InputStream nifFileStream = converter.convert2Nif(inStream, "en");
			 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(nifFileStream, "UTF-8"));
			 String line = bufferedReader.readLine();
		        while(line != null){
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
		}
	}
}
