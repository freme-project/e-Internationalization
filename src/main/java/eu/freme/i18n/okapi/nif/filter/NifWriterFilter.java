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
package eu.freme.i18n.okapi.nif.filter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextPart;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.freme.i18n.okapi.nif.its.ITSAnnotAttribute;
import eu.freme.i18n.okapi.nif.its.ITSAnnotation;
import eu.freme.i18n.okapi.nif.its.ITSAnnotationsHelper;
import eu.freme.i18n.okapi.nif.step.NifParameters;

/**
 * Writer filter class for NIF documents.
 */
public class NifWriterFilter extends AbstractNifWriterFilter {

	/** The object used for the context string building. */
	private StringBuilder referenceContextText;

	/**
	 * Line break used for separating different text units when building the
	 * reference context.
	 */
	private String lineBreak = " ";

	/** Helper class for marker management. */
	private NifMarkerHelper markerHelper;

	/** map matching a locale to the appropriate target reference context. */
	private Map<String, StringBuilder> locale2TargetRefCtxMap;

	/**
	 * Constructor.
	 * 
	 * @param params
	 *            the parameters
	 * @param sourceLocale
	 *            the source locale
	 */
	public NifWriterFilter(NifParameters params, LocaleId sourceLocale) {

		super(params, sourceLocale);
		markerHelper = new NifMarkerHelper(this);
	}

	/**
	 * Constructor.
	 */
	public NifWriterFilter() {

		super(new NifParameters(), null);
		markerHelper = new NifMarkerHelper(this);
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

		// init the start index for this text unit
		String sourceText = getText(textUnit.getSource(), sourceLocale, false);
		// if the reference context text is not empty, then append a line break.
		if (referenceContextText.length() > 0 && !sourceText.isEmpty()) {
			referenceContextText.append(lineBreak);
		}
		int startIndex = referenceContextText.length();
		// append the source text of this text unit to the reference context
		// text
		referenceContextText.append(sourceText);
		// create a resource for this text unit in the NIF model
		if (sourceText.length() > 0) {
			Resource textUnitResource = createTextUnitResource(sourceText, sourceLocale,
					startIndex, startIndex + sourceText.length(),
					textUnit.getId(), false);

			manageItsAnnotations(textUnitResource, textUnit,
					textUnit.getSource());

			// if target locales exist and related target texts exist as well,
			// create a target property for the current NIF resource
			Set<LocaleId> targetLocales = textUnit.getTargetLocales();
			if (targetLocales != null) {
				for (LocaleId targetLocale : targetLocales) {

					// //
					if (!locale2TargetRefCtxMap.containsKey(targetLocale
							.getLanguage())) {
						locale2TargetRefCtxMap.put(targetLocale.getLanguage(),
								new StringBuilder());
					} else {
						locale2TargetRefCtxMap.get(targetLocale.getLanguage())
								.append(lineBreak);
					}
					int startTrgtIdx = locale2TargetRefCtxMap.get(
							targetLocale.getLanguage()).length();
					String targetText = getText(
							textUnit.getTarget(targetLocale), targetLocale,
							true);
					if (targetText.length() > 0) {
						locale2TargetRefCtxMap.get(targetLocale.getLanguage())
								.append(targetText);
						Resource targetRes = createTextUnitResource(targetText,
								targetLocale, startTrgtIdx, startTrgtIdx
										+ targetText.length(),
								textUnit.getId(), true);
						// //

						addTranslation(textUnitResource, targetRes);
						manageItsAnnotations(textUnitResource, textUnit,
								textUnit.getTarget(targetLocale));
					}
				}
			}
		}
	}

	/**
	 * Gets the text from an Okapi TextContainer object.
	 * 
	 * @param tc
	 *            the Okapi TextContainer
	 * @return the text from the Okapi TextContainer
	 */
	private String getText(final TextContainer tc, LocaleId locale,
			final boolean isTarget) {

		StringBuilder sb = new StringBuilder();
		markerHelper.clear();
		for (TextPart part : tc.getParts()) {
			sb.append(markerHelper.toString(part.getContent(), locale, isTarget));
		}
		String text = sb.toString();
		if (!Normalizer.isNormalized(text, Normalizer.Form.NFC)) {
			text = Normalizer.normalize(text, Normalizer.Form.NFC);
		}
		return text;
	}

	/**
	 * Adds a target property to a specific resource.
	 * 
	 * @param resource
	 *            the NIF resource
	 * @param target
	 *            the NIF target resource
	 */
	private void addTranslation(Resource resource, Resource target) {

		// Property targetProp = model.createProperty(RDFConstants.itsrdfPrefix,
		// "target");
		// if (targetLocale != null) {
		// resource.addProperty(targetProp, targetText,
		// targetLocale.getLanguage());
		// } else {
		// resource.addProperty(targetProp, targetText);
		// }
		Property targetProp = model.createProperty(RDFConstants.itsrdfPrefix,
				"target");
		resource.addProperty(targetProp, target);
	}

	/**
	 * Creates a NIF resource for a specific text unit.
	 * 
	 * @param text
	 *            the text
	 * @param locale
	 *            the locale
	 * @param startIdx
	 *            the start index
	 * @param endIdx
	 *            the end index
	 * @param unitId
	 *            the text unit ID
	 * @param isTarget
	 *            a boolean stating if this text is from a target.
	 * @return the created resource.
	 */
	private Resource createTextUnitResource(String text, LocaleId locale,
			int startIdx, int endIdx, String unitId, boolean isTarget) {

		// creates the NIF resource
		Resource resource = model.createResource(getURI(startIdx, endIdx,
				locale, isTarget));

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
			resource.addProperty(anchorOf, text, sourceLocale.getLanguage());
		} else {
			resource.addProperty(anchorOf, text);
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

		if (unitId != null) {
			// Adds the text unit ID by using the identifier property
			Property identifier = model.createProperty(RDFConstants.dcPrefix
					+ "identifier");
			resource.addProperty(identifier, model.createLiteral(unitId));
		}
		return resource;
	}

	/**
	 * Gets the URI based on given start and end indices.
	 * 
	 * @param startIdx
	 *            the start index
	 * @param endIdx
	 *            the end index
	 * @param locale
	 *            the locale
	 * @param isTarget
	 *            a boolean stating if the URI is for a target text
	 * @return the URI string
	 */
	private String getURI(int startIdx, int endIdx, LocaleId locale,
			boolean isTarget) {

		String uriTargetLoc = "";
		if (isTarget) {
			uriTargetLoc = "target-" + locale.getLanguage();
		}
		return uriPrefix
				+ (originalDocName != null ? originalDocName + "/" : "")
				+ uriTargetLoc + URI_CHAR_OFFSET + startIdx + "," + endIdx;
	}

	/**
	 * Creates the context reference resource, containing text from all text
	 * units.
	 * 
	 * @param text
	 *            the text for the context reference resource.
	 * @param locale
	 *            the locale
	 * @param isTarget
	 *            a boolean stating if the text if from a target
	 * @return the context reference resource.
	 */
	private Resource createContextResource(final String text, LocaleId locale,
			boolean isTarget) {

		// The URI offset for the context reference resource is 0-total text
		// length.
		String contextURI = getURI(0, text.length(), locale, isTarget);
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
				"referenceContext");
		while (iterator.hasNext()) {
			Resource currRes = iterator.next();
			if (uriPrefixMacthes(contextURI, currRes.getURI())) {
				if (!currRes.getURI().equals(contextURI)) {
					Property typeProp = model
							.createProperty(RDFConstants.typePrefix);
					currRes.addProperty(refContext,
							model.createResource(contextURI));
					currRes.addProperty(
							typeProp,
							model.createResource(RDFConstants.nifPrefix
									+ "Phrase"));
				} else {
					// if the current resource has the same URI as the context
					// URI,
					// then it is the reference context resource --> remove the
					// "anchorOf" property
					currRes.removeAll(anchorOf);
				}
			}
		}
	}

	private boolean uriPrefixMacthes(String uri1, String uri2) {

		int offsetCharIndex1 = uri1.indexOf(URI_CHAR_OFFSET);
		int offsetCharIndex2 = uri2.indexOf(URI_CHAR_OFFSET);

		return uri1.substring(0, offsetCharIndex1).equals(
				uri2.substring(0, offsetCharIndex2));
	}

	/**
	 * Processes the end of the document: creates the reference context resource
	 * and saves the file into the file system.
	 */
	public void processEndDocument() {

		createContextResource(referenceContextText.toString(), sourceLocale,
				false);
		if (!locale2TargetRefCtxMap.isEmpty()) {
			for (Entry<String, StringBuilder> entry : locale2TargetRefCtxMap
					.entrySet()) {
				createContextResource(entry.getValue().toString(),
						new LocaleId(entry.getKey()), true);
			}
		}
		close();
		referenceContextText = null;
		locale2TargetRefCtxMap = null;
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
		locale2TargetRefCtxMap = new HashMap<String, StringBuilder>();
	}

	/**
	 * Manages the ITS annotations for a specific text unit.
	 * 
	 * @param resource
	 *            the NIF resource
	 * @param tu
	 *            the text unit
	 * @param container
	 *            the text container
	 */
	private void manageItsAnnotations(Resource resource, ITextUnit tu,
			TextContainer container) {

		List<ITSAnnotation> itsAnnotations = new ArrayList<ITSAnnotation>();
		itsAnnotations.addAll(retrieveItsAnnotations(tu.getAnnotations()));
		itsAnnotations
				.addAll(retrieveItsAnnotations(container.getAnnotations()));
		// for (TextPart part : container.getParts()) {
		// if (part.getContent().getCodes() != null) {
		// for (Code code : part.getContent().getCodes()) {
		// itsAnnotations.addAll(retrieveItsAnnotations(code
		// .getGenericAnnotations()));
		// }
		// }
		// }
		addItsAnnotations(resource, itsAnnotations);
	}

	/**
	 * Retrieves the ITS annotations from a list of generic annotations.
	 * 
	 * @param annotations
	 *            the list of generic annotations
	 * @return the list of ITS annotations.
	 */
	private List<ITSAnnotation> retrieveItsAnnotations(
			GenericAnnotations annotations) {
		List<ITSAnnotation> itsAnnotations = new ArrayList<ITSAnnotation>();
		if (annotations != null) {
			Iterator<GenericAnnotation> annsIt = annotations.iterator();
			while (annsIt.hasNext()) {
				GenericAnnotation annot = annsIt.next();
				itsAnnotations
						.add(ITSAnnotationsHelper.getITSAnnotation(annot));
			}
		}
		return itsAnnotations;
	}

	/**
	 * Retrieves the ITS annotations from a list of annotations.
	 * 
	 * @param annotations
	 *            the list of annotations
	 * @return the list ITS annotations.
	 */
	private List<ITSAnnotation> retrieveItsAnnotations(
			Iterable<IAnnotation> annotations) {

		List<ITSAnnotation> itsAnnotations = new ArrayList<ITSAnnotation>();
		Iterator<IAnnotation> annIterator = annotations.iterator();
		while (annIterator.hasNext()) {
			IAnnotation annot = annIterator.next();
			if (annot instanceof GenericAnnotations) {
				for (GenericAnnotation genAnn : ((GenericAnnotations) annot)
						.getAllAnnotations()) {
					itsAnnotations.add(ITSAnnotationsHelper
							.getITSAnnotation((GenericAnnotation) genAnn));
				}
			}
		}
		return itsAnnotations;
	}

	/**
	 * Adds a list of ITS annotations to a specific NIF resource.
	 * 
	 * @param resource
	 *            the NIF resource
	 * @param itsAnnotations
	 *            the list of ITS annotations.
	 */
	private void addItsAnnotations(Resource resource,
			List<ITSAnnotation> itsAnnotations) {

		// List<ITSAnnotation> itsAnnotations = new ArrayList<ITSAnnotation>();
		// Annotations annotations = textContainer.getAnnotations();
		// Iterator<IAnnotation> annIterator = annotations.iterator();
		// while (annIterator.hasNext()) {
		// IAnnotation annot = annIterator.next();
		// if (annot instanceof GenericAnnotations) {
		// for (GenericAnnotation genAnn : ((GenericAnnotations) annot)
		// .getAllAnnotations()) {
		// itsAnnotations.add(ITSAnnotationsHelper
		// .getITSAnnotation((GenericAnnotation) genAnn));
		// }
		// }
		// }
		Property itsProp = null;
		for (ITSAnnotation ann : itsAnnotations) {

			for (ITSAnnotAttribute attr : ann.getAttributes()) {
				itsProp = model.createProperty(RDFConstants.itsrdfPrefix
						+ attr.getName());
				RDFNode node = createItsRdfNode(attr);
				resource.addProperty(itsProp, node);
			}
		}

	}

	// private void addItsAnnotations(Resource textUnitResource, String tuId) {
	//
	// if (textUnit2ItsAnnots.containsKey(tuId)
	// && !textUnit2ItsAnnots.get(tuId).isEmpty()) {
	// Property itsProp = null;
	// for (ITSAnnotation ann : textUnit2ItsAnnots.get(tuId)) {
	//
	// for (ITSAnnotAttribute attr : ann.getAttributes()) {
	// itsProp = model.createProperty(RDFConstants.itsrdfPrefix
	// + attr.getName());
	// RDFNode node = createItsRdfNode(attr);
	// textUnitResource.addProperty(itsProp, node);
	// }
	// }
	// }
	// }

	/**
	 * Creates the appropriate its-rdf node for the specific ITS annotation
	 * attribute
	 * 
	 * @param attribute
	 *            the attribute
	 * @return the its-rdf node.
	 */
	private RDFNode createItsRdfNode(ITSAnnotAttribute attribute) {

		RDFNode rdfNode = null;
		switch (attribute.getType()) {
		case ITSAnnotAttribute.BOOLEAN_TYPE:
			rdfNode = model.createLiteral(attribute.getValue().toString()
					.toLowerCase());
			break;
		case ITSAnnotAttribute.DOUBLE_TYPE:
			rdfNode = model.createTypedLiteral(attribute.getValue(),
					XSDDatatype.XSDdouble);
			break;
		case ITSAnnotAttribute.INTEGER_TYPE:
			rdfNode = model.createTypedLiteral(attribute.getValue(),
					XSDDatatype.XSDinteger);
			break;
		case ITSAnnotAttribute.STRING_TYPE:
			rdfNode = model.createLiteral(attribute.getValue().toString());
			break;
		case ITSAnnotAttribute.UNISGNED_INTEGER_TYPE:
			rdfNode = model.createTypedLiteral(attribute.getValue(),
					XSDDatatype.XSDunsignedInt);
			break;
		case ITSAnnotAttribute.IRI_STRING_TYPE:
			rdfNode = model.createResource(attribute.getValue().toString());
			break;
		default:
			break;
		}
		return rdfNode;
	}

	/**
	 * Creates a NIF resource containing in line annotations.
	 * 
	 * @param annotatedText
	 *            the annotated text
	 * @param startIdx
	 *            the start index
	 * @param locale
	 *            the locale
	 * @param isTarget
	 *            a boolean stating if the text is from a target
	 * @param annotations
	 *            the list of annotations
	 */
	public void createResourceForInlineAnnotation(String annotatedText,
			int startIdx, LocaleId locale, boolean isTarget,
			GenericAnnotations annotations) {
		int actualStartIdx = 0;
		if (isTarget && locale2TargetRefCtxMap.get(locale) != null) {
			actualStartIdx = startIdx
					+ locale2TargetRefCtxMap.get(locale).length();
		} else {
			actualStartIdx = startIdx + referenceContextText.length();
		}
		Resource resource = createTextUnitResource(annotatedText, locale,
				actualStartIdx, actualStartIdx + annotatedText.length(), null,
				isTarget);
		addItsAnnotations(resource, retrieveItsAnnotations(annotations));

	}
}
