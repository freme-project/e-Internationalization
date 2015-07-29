package eu.freme.i18n.okapi.nif.filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Set;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
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

/**
 * Writer filter class for NIF documents. It handles filter events from Okapi
 * pipeline and creates a NIF file.
 */
public class NifWriterFilter implements IFilterWriter {

	/**
	 * the default URI prefix for NIF resources. It is only used if a custom URI
	 * is not specified.
	 */
	private final static String DEF_URI_PREFIX = "http://example.org/";

	/** The string preceding the offset in the URI string. */
	private final static String URI_CHAR_OFFSET = "#char=";

	/** The original document name. */
	private String originalDocName;

	/** The parameters set for this file. */
	private NifParameters params;

	/** The path where the output file is saved. */
	private String fwOutputPath;

	/** The output stream. */
	private OutputStream fwOutputStream;

	/** The Jena model used for managing the NIF file. */
	private Model model;

	/** The object used for the context string building. */
	private StringBuilder referenceContextText;

	/** The source locale. */
	private LocaleId sourceLocale;

	/** The URI prefix for NIF resources. */
	private String uriPrefix;

	/**
	 * Line break used for separating different text units when building the
	 * reference context.
	 */
	private String lineBreak = " ";

	/** Helper class for marker management. */
	private NifMarkerHelper markerHelper;

	/**
	 * Constructor.
	 * 
	 * @param params
	 *            the parameters
	 * @param sourceLocale
	 *            the source locale
	 */
	public NifWriterFilter(NifParameters params, LocaleId sourceLocale) {

		this.params = params;
		this.sourceLocale = sourceLocale;
		markerHelper = new NifMarkerHelper();
	}

	/**
	 * Constructor.
	 */
	public NifWriterFilter() {

		params = new NifParameters();
		markerHelper = new NifMarkerHelper();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.okapi.common.filterwriter.IFilterWriter#getName()
	 */
	@Override
	public String getName() {
		return getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.okapi.common.filterwriter.IFilterWriter#setOptions(net.sf.okapi
	 * .common.LocaleId, java.lang.String)
	 */
	@Override
	public void setOptions(LocaleId locale, String defaultEncoding) {
		// do nothing

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.okapi.common.filterwriter.IFilterWriter#setOutput(java.lang.String
	 * )
	 */
	@Override
	public void setOutput(String path) {
		this.fwOutputPath = path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.okapi.common.filterwriter.IFilterWriter#setOutput(java.io.OutputStream
	 * )
	 */
	@Override
	public void setOutput(OutputStream output) {
		this.fwOutputStream = output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.okapi.common.filterwriter.IFilterWriter#handleEvent(net.sf.okapi
	 * .common.Event)
	 */
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

	/**
	 * Processes a single text unit. For each text unit a new resource is
	 * created in NIF.
	 * 
	 * @param textUnit
	 *            the text unit.
	 */
	public void processTextUnit(ITextUnit textUnit) {

		// if the reference context text is not empty, then append a line break.
		if (referenceContextText.length() > 0) {
			referenceContextText.append(lineBreak);
		}
		// init the start index for this text unit
		int startIndex = referenceContextText.length();
		String sourceText = getText(textUnit.getSource());
		// append the source text of this text unit to the reference context
		// text
		referenceContextText.append(sourceText);
		// create a resource for this text unit in the NIF model
		Resource textUnitResource = createTextUnitResource(sourceText,
				startIndex, startIndex + sourceText.length(), textUnit.getId());

		// if target locales exist and related target texts exist as well,
		// create a target property for the current NIF resource
		Set<LocaleId> targetLocales = textUnit.getTargetLocales();
		if (targetLocales != null) {
			for (LocaleId targetLocale : targetLocales) {

				addTranslation(textUnitResource,
						getText(textUnit.getTarget(targetLocale)), targetLocale);
			}
		}
	}

	/**
	 * Adds a target property to a specific resource.
	 * 
	 * @param resource
	 *            the NIF resource
	 * @param targetText
	 *            the target text
	 * @param targetLocale
	 *            the target locale.
	 */
	private void addTranslation(Resource resource, String targetText,
			LocaleId targetLocale) {

		Property targetProp = model.createProperty(RDFConstants.itsrdfPrefix,
				"target");
		if (targetLocale != null) {
			resource.addProperty(targetProp, targetText,
					targetLocale.getLanguage());
		} else {
			resource.addProperty(targetProp, targetText);
		}
	}

	/**
	 * Creates a NIF resource for a specific text unit.
	 * 
	 * @param source
	 *            the text unit text
	 * @param startIdx
	 *            the start index
	 * @param endIdx
	 *            the end index
	 * @param unitId
	 *            the text unit ID
	 * @return the created resource.
	 */
	private Resource createTextUnitResource(String source, int startIdx,
			int endIdx, String unitId) {

		// creates the NIF resource
		Resource resource = model.createResource(getURI(startIdx, endIdx));

		// adds following NIF types: String and RFC5147String
		Property type = model.createProperty(RDFConstants.typePrefix);
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "String"));
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "RFC5147String"));

		// adds the text with the anchorOf property
		Property anchorOf = model.createProperty(RDFConstants.nifPrefix,
				"anchorOf");
		if (sourceLocale != null) {
			resource.addProperty(anchorOf, source, sourceLocale.getLanguage());
		} else {
			resource.addProperty(anchorOf, source);
		}

		// adds start and end index properties
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

		// Adds the text unit ID by using the identifier property
		Property identifier = model.createProperty(RDFConstants.dcPrefix
				+ "identifier");
		resource.addProperty(identifier, model.createLiteral(unitId));

		return resource;
	}

	/**
	 * Gets the URI based on given start and end indices.
	 * 
	 * @param startIdx
	 *            the start index
	 * @param endIdx
	 *            the end index
	 * @return the URI string
	 */
	private String getURI(int startIdx, int endIdx) {

		return uriPrefix + (originalDocName != null ? originalDocName : "")
				+ URI_CHAR_OFFSET + startIdx + "," + endIdx;
	}

	/**
	 * Gets the text from an Okapi TextContainer object.
	 * 
	 * @param tc
	 *            the Okapi TextContainer
	 * @return the text from the Okapi TextContainer
	 */
	private String getText(final TextContainer tc) {

		StringBuilder sb = new StringBuilder();
		for (TextPart part : tc.getParts()) {
			sb.append(markerHelper.toString(part.getContent()));
		}
		return sb.toString();
	}

	/**
	 * Creates the context reference resource, containing text from all text
	 * units.
	 * 
	 * @param text
	 *            the text for the context reference resource.
	 * @return the context reference resource.
	 */
	private Resource createContextResource(final String text) {

		// The URI offset for the context reference resource is 0-total text
		// length.
		String contextURI = getURI(0, text.length());
		// Adds the context reference property to all existing text unit
		// resources in the model
		addContextReference(contextURI);

		// creates the reference context resource
		Resource resource = model.createResource(contextURI);

		// Adds following types: String, Context, RFC5147String
		Property type = model.createProperty(RDFConstants.typePrefix);
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "String"));
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "Context"));
		resource.addProperty(type,
				model.createResource(RDFConstants.nifPrefix + "RFC5147String"));

		// Adds the text with the isString property
		if (text.length() > 0) {
			if (sourceLocale == null) {
				resource.addProperty(
						model.createProperty(RDFConstants.nifPrefix
								+ "isString"),
						model.createLiteral(referenceContextText.toString()));
			} else {
				resource.addProperty(
						model.createProperty(RDFConstants.nifPrefix
								+ "isString"),
						model.createLiteral(text, sourceLocale.getLanguage()));
			}
			// Adds begin and end indices
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
		return resource;
	}

	/**
	 * Adds the ReferenceContext property to all text unit resources created so
	 * far.
	 * 
	 * @param contextURI
	 *            the reference context URI.
	 */
	private void addContextReference(String contextURI) {

		// Retrieve all resources having "anchorOf" property. For each of them
		// add the "ReferenceContext" property
		Property anchorOf = model.createProperty(RDFConstants.nifPrefix,
				"anchorOf");
		ResIterator iterator = model.listResourcesWithProperty(anchorOf);
		Property refContext = model.createProperty(RDFConstants.nifPrefix,
				"ReferenceContext");
		while (iterator.hasNext()) {
			Resource currRes = iterator.next();
			if (!currRes.getURI().equals(contextURI)) {
				Property typeProp = model
						.createProperty(RDFConstants.typePrefix);
				currRes.addProperty(refContext, contextURI);
				currRes.addProperty(typeProp,
						model.createResource(RDFConstants.nifPrefix + "Phrase"));
			} else {
				// if the current resource has the same URI as the context URI,
				// then it is the reference context resource --> remove the
				// "anchorOf" property
				currRes.removeAll(anchorOf);
			}
		}
	}

	/**
	 * Processes the end of the document: creates the reference context resource
	 * and saves the file into the file system.
	 */
	public void processEndDocument() {

		createContextResource(referenceContextText.toString());
		close();
	}

	/**
	 * Processes the start of the document. It initializes some fields and
	 * creates and initializes the Jena model.
	 * 
	 * @param resource
	 *            the resource representing the start document
	 */
	public void processStartDocument(StartDocument resource) {

		String resourceName = resource.getName();
		if (resource.getLocale() != null) {
			sourceLocale = resource.getLocale();
		}
		if (resourceName != null) {
			int lastSepIdx = resourceName.lastIndexOf("/");
			if (lastSepIdx != -1 && (lastSepIdx + 1) < resourceName.length()) {
				originalDocName = resourceName.substring(lastSepIdx + 1);
			}
		}
		uriPrefix = params.getNifURIPrefix();
		if (uriPrefix == null || uriPrefix.isEmpty()) {
			uriPrefix = DEF_URI_PREFIX;
		}

		create();

	}

	/**
	 * Creates the Jena model and initializes the reference context text
	 * builder.
	 */
	public void create() {

		model = ModelFactory.createDefaultModel();
		model.setNsPrefix("nif", RDFConstants.nifPrefix);
		model.setNsPrefix("xsd", RDFConstants.xsdPrefix);
		model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
		model.setNsPrefix("dc", RDFConstants.dcPrefix);
		referenceContextText = new StringBuilder();
	}

	/**
	 * Gets the appropriate output file extension based on the RDF serialization format
	 * chosen for the NIF file.
	 * 
	 * @return the file extension
	 */
	private String getOutFileExtension() {
		String nifLanguage = params.getNifLanguage();
		String ext = ".rdf";
		if (nifLanguage != null) {
			if (nifLanguage.equals(RDFSerialization.TURTLE.toRDFLang())) {
				ext = ".ttl";
			} else if (nifLanguage.equals(RDFSerialization.JSON_LD.toRDFLang())) {
				ext = ".json";
			}
		}
		return ext;
	}

	/**
	 * Closes the document by saving the NIF file.
	 * 
	 * @see net.sf.okapi.common.filterwriter.IFilterWriter#close()
	 */
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
					outputPath = new File(outDirPath, originalDocName
							+ getOutFileExtension()).toURI().toString();
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

			if (outputStream != null) {
				final String nifLang = params.getNifLanguage();
				OutputStreamWriter writer = new OutputStreamWriter(
						outputStream, Charset.forName("UTF-8").newEncoder());
				if (nifLang != null && !nifLang.isEmpty()) {
					System.out.println(nifLang);
					model.write(writer, nifLang);
				} else {
					model.write(writer);
				}
			}

		} catch (IOException e) {
			throw new OkapiIOException("Error while saving the NIF file.", e);
		} catch (URISyntaxException e) {
			throw new OkapiIOException("invalid output file URI sintax.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.filterwriter.IFilterWriter#getParameters()
	 */
	@Override
	public IParameters getParameters() {
		return params;
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.filterwriter.IFilterWriter#setParameters(net.sf.okapi.common.IParameters)
	 */
	@Override
	public void setParameters(IParameters params) {
		if (!(params instanceof NifParameters)) {
			throw new IllegalArgumentException("Received params of type "
					+ params.getClass().getName()
					+ ". Only NifParameters accepted.");
		}
		this.params = (NifParameters) params;
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.filterwriter.IFilterWriter#cancel()
	 */
	@Override
	public void cancel() {
		// do nothing

	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.filterwriter.IFilterWriter#getEncoderManager()
	 */
	@Override
	public EncoderManager getEncoderManager() {
		// do nothing
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.filterwriter.IFilterWriter#getSkeletonWriter()
	 */
	@Override
	public ISkeletonWriter getSkeletonWriter() {
		// do nothing
		return null;
	}

}
