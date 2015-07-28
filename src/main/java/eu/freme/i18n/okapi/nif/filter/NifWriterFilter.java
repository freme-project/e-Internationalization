package eu.freme.i18n.okapi.nif.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.freme.i18n.okapi.nif.filter.RDFConstants.RDFSerialization;
import eu.freme.i18n.okapi.nif.step.NifParameters;

public class NifWriterFilter implements IFilterWriter {

	private final static String URI_PREFIX = "http://example.org/";
	private final static String URI_CHAR_OFFSET = "#char=";
	private String originalDocName;
	private NifParameters params;
	private String fwOutputPath;
	private OutputStream fwOutputStream;
	private Model model;
	private Resource docResource;
	private StringBuilder wholeContent;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;

//	private String lineBreak = System.getProperty("line.separator");
	private String lineBreak = " ";

	private NifMarkerHelper markerHelper;

	public NifWriterFilter(NifParameters params, LocaleId sourceLocale,
			LocaleId targetLocale) {

		this.params = params;
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
		markerHelper = new NifMarkerHelper();
	}

	public NifWriterFilter() {

		params = new NifParameters();
		markerHelper = new NifMarkerHelper();
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public void setOptions(LocaleId locale, String defaultEncoding) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutput(String path) {
		this.fwOutputPath = path;
	}

	@Override
	public void setOutput(OutputStream output) {
		this.fwOutputStream = output;
	}

	@Override
	public Event handleEvent(Event event) {
		switch (event.getEventType()) {
		case START_DOCUMENT:
			processStartDocument((StartDocument) event.getResource());
			break;
		case END_DOCUMENT:
			processEndDocument();
			break;
		case TEXT_UNIT:
			processTextUnit(event.getTextUnit());
			break;
		case START_SUBDOCUMENT:
		case END_SUBDOCUMENT:
		case START_GROUP:
		case START_SUBFILTER:
		case END_GROUP:
		case END_SUBFILTER:
			break;
		default:
			// Do nothing
			break;
		}
		return event;
	}

	public void processTextUnit(ITextUnit textUnit) {

		if (wholeContent.length() > 0) {
			wholeContent.append(lineBreak);
		}
		int startIndex = wholeContent.length();
		String sourceText = getText(textUnit.getSource());
		wholeContent.append(sourceText);
		if (textUnit.getTarget(targetLocale) != null) {
			String targetText = getText(textUnit.getTarget(targetLocale));
			if (targetText != null && !targetText.isEmpty()) {
				createTranslatedTextUnit(sourceText, targetText, startIndex,
						startIndex + sourceText.length(), textUnit.getId());
			}
		}
	}

	private void createTranslatedTextUnit(String source, String target,
			int startIdx, int endIdx, String unitId) {

		Resource resource = model.createResource(getURI(startIdx, endIdx));
		Property type = model.createProperty(RDFConstants.typePrefix);
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "String"));
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "RFC5147String"));
		Property anchorOf = model.createProperty(RDFConstants.nifPrefix,
				"anchorOf");
		if (sourceLocale != null) {
			resource.addProperty(anchorOf, source, sourceLocale.getLanguage());
		} else {
			resource.addProperty(anchorOf, source);
		}

		Property targetProp = model.createProperty(RDFConstants.itsrdfPrefix,
				"target");
		if (targetLocale != null) {
			resource.addProperty(targetProp, target, targetLocale.getLanguage());
		} else {
			resource.addProperty(targetProp, target);
		}

		Literal beginIndex = model.createTypedLiteral(new Integer(startIdx),
				XSDDatatype.XSDnonNegativeInteger);
		resource.addProperty(
				model.createProperty(RDFConstants.nifPrefix + "beginIndex"),
				beginIndex);
		Literal endIndex = model.createTypedLiteral(new Integer(endIdx),
				XSDDatatype.XSDnonNegativeInteger);
		resource.addProperty(
				model.createProperty(RDFConstants.nifPrefix + "endIndex"),
				endIndex);
//		resource.addProperty(model.createProperty(RDFConstants.nifPrefix + "wasConvertedFrom"), URI_PREFIX + originalDocName + "/unit=" + unitId );

	}

	private String getURI(int startIdx, int endIdx) {
		return URI_PREFIX + originalDocName + URI_CHAR_OFFSET + startIdx + ","
				+ endIdx;
	}

	private String getText(final TextContainer tc) {

		StringBuilder sb = new StringBuilder();
		for (TextPart part : tc.getParts()) {
			sb.append(markerHelper.toString(part.getContent()));
		}
		return sb.toString();
	}

	private Resource createContextResource(final String text) {

		String contextURI = getURI(0, text.length());
		addContextReference(contextURI);

		Resource resource = model.createResource(contextURI);
		Property type = model.createProperty(RDFConstants.typePrefix);
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "String"));
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "Context"));
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "RFC5147String"));

		if (text.length() > 0) {
			if (sourceLocale == null) {
				resource.addProperty(
						model.createProperty(RDFConstants.nifPrefix
								+ "isString"),
						model.createLiteral(wholeContent.toString()));
			} else {
				resource.addProperty(
						model.createProperty(RDFConstants.nifPrefix
								+ "isString"),
						model.createLiteral(text, sourceLocale.getLanguage()));
			}
			Literal beginIndex = model.createTypedLiteral(new Integer(0),
					XSDDatatype.XSDnonNegativeInteger);
			resource.addProperty(
					model.createProperty(RDFConstants.nifPrefix + "beginIndex"),
					beginIndex);
			Literal endIndex = model.createTypedLiteral(
					new Integer(text.length()),
					XSDDatatype.XSDnonNegativeInteger);
			resource.addProperty(
					model.createProperty(RDFConstants.nifPrefix + "endIndex"),
					endIndex);

		}
		// if (params.getNifLanguage() == null) {
		// resource.addProperty(
		// model.createProperty(RDFConstants.nifPrefix + "isString"),
		// model.createLiteral(text));
		// } else {
		// resource.addProperty(
		// model.createProperty(RDFConstants.nifPrefix + "isString"),
		// model.createLiteral(text, params.getNifLanguage()));
		// }
		return resource;
	}

	private void addContextReference(String contextURI) {

//		ResIterator iterator = model.listSubjectsWithProperty(
//				model.createProperty(RDFConstants.nifPrefix, "type"), model.createResource(RDFConstants.nifPrefix + "Phrase"));
		Property anchorOf = model.createProperty(RDFConstants.nifPrefix, "anchorOf");
		ResIterator iterator = model.listResourcesWithProperty(anchorOf);
		Property refContext = model.createProperty(RDFConstants.nifPrefix,
				"ReferenceContext");
		while (iterator.hasNext()) {
			Resource currRes = iterator.next();
			if(!currRes.getURI().equals(contextURI)){
////				currRes.addProperty(typeProp, model.createResource(RDFConstants.nifPrefix +"String"));
////				currRes.addProperty(typeProp, model.createResource(RDFConstants.nifPrefix +"RFC5147String"));
////				currRes.addProperty(typeProp, model.createResource(RDFConstants.nifPrefix +"Context"));
//			} else {
				Property typeProp = model.createProperty(RDFConstants.typePrefix); 
				currRes.addProperty(refContext, contextURI);
				currRes.addProperty(typeProp,
						model.createResource(RDFConstants.nifPrefix + "Phrase"));
			} else {
				currRes.removeAll(anchorOf);
			}
		}
	}

	public void processEndDocument() {

		createContextResource(wholeContent.toString());
		close();
	}

	public void processStartDocument(StartDocument resource) {

		System.out.println("----- Start Document: resource.getName = "
				+ resource.getName());
		String resourceName = resource.getName();
		if (resourceName != null) {
			int lastSepIdx = resourceName.lastIndexOf("/");
			if (lastSepIdx != -1 && (lastSepIdx + 1) < resourceName.length()) {
				originalDocName = resourceName.substring(lastSepIdx + 1) ;
			}
		}
		create();

	}

	public void create() {

		model = ModelFactory.createDefaultModel();
		model.setNsPrefix("nif", RDFConstants.nifPrefix);
		model.setNsPrefix("xsd", RDFConstants.xsdPrefix);
		model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
		wholeContent = new StringBuilder();
	}
	
	private String getOutFileExtension(){
		String nifLanguage = params.getNifLanguage();
		String ext = ".rdf";
		if(nifLanguage != null){
			if(nifLanguage.equals(RDFSerialization.TURTLE.toRDFLang())){
				ext = ".ttl";
			} else if(nifLanguage.equals(RDFSerialization.JSON_LD.toRDFLang())){
				ext = ".json";
			}
		}
		return ext;
	}

	@Override
	public void close() {

		OutputStream outputStream = null;
		try {

			String outputPath = params.getOutputURI();
			if (outputPath == null || outputPath.isEmpty()) {
				String outDirPath = params.getOutBasePath();
				if (outDirPath == null || outDirPath.isEmpty()) {
					if (fwOutputPath != null) {
						outputPath = fwOutputPath;
					}
				} else {
					outputPath = new File(outDirPath, originalDocName + getOutFileExtension()).toURI().toString();
				}
			}
			
			if (outputPath != null) {
				Util.createDirectories(outputPath);
				File file = new File(new URI(outputPath));
				if (file.exists()) {
					file.delete();
				}
				System.out.println(outputPath);
				file.createNewFile();
				outputStream = new FileOutputStream(file);

			} else {
				outputStream = fwOutputStream;
			}
			final String nifLang = params.getNifLanguage();
			// FileWriter writer = new FileWriter(outputPath);
//			if (nifLang != null && !nifLang.isEmpty()) {
//				model.write(outputStream, nifLang, null);
//			} else {
//				model.write(outputStream);
//			}
			
			OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8").newEncoder());
			if (nifLang != null && !nifLang.isEmpty()) {
				System.out.println(nifLang);
				model.write(writer, nifLang);
//				model.write(outputStream, nifLang, null);
			} else {
				model.write(writer);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		if (!(params instanceof NifParameters)) {
			throw new IllegalArgumentException("Received params of type "
					+ params.getClass().getName()
					+ ". Only NifParameters accepted.");
		}
		this.params = (NifParameters) params;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public EncoderManager getEncoderManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter() {
		// TODO Auto-generated method stub
		return null;
	}

}
