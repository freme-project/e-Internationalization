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
package eu.freme.i18n.okapi.nif.step;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;

/**
 * Parameters used in all NIF steps.
 */
public class NifParameters extends StringParameters {

	/** The output file URI constant. */
	public static final String OUTPUT_URI = "outputURI";

	/** The output base path constant. */
	public static final String OUTPUT_BASE_PATH = "outBasePath";

	/** The NIF serialization language constant. */
	public static final String NIF_LANGUAGE = "nifLanguage";

	/** The URI prefix to be used for NIF resources. */
	public static final String NIF_URI_PREFIX = "nifUriPrefix";

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.okapi.common.StringParameters#reset()
	 */
	@Override
	public void reset() {
		setOutputURI("");
		setNifLanguage("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.okapi.common.BaseParameters#getParametersDescription()
	 */
	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(OUTPUT_URI, "Path of the NIF file", "NIF file Path");
		desc.add(
				NIF_LANGUAGE,
				"The format used for NIF serialization. Allowed values: RDF, TTL, JSON. The default is RDF.",
				"NIF serialization format.");
		desc.add(OUTPUT_BASE_PATH, "Directory of the NIF file.",
				"NIF file directory.");
		desc.add(NIF_URI_PREFIX, "NIF URI prefix",
				"URI prefix to be used for resources in the NIF file.");
		return desc;
	}

	/**
	 * Gets the output file URI.
	 * 
	 * @return the output file URI.
	 */
	public String getOutputURI() {
		return getString(OUTPUT_URI);
	}

	/**
	 * Gets the NIF serialization language.
	 * 
	 * @return the NIF serialization language.
	 */
	public String getNifLanguage() {
		return getString(NIF_LANGUAGE);
	}

	/**
	 * Sets the output file URI.
	 * 
	 * @param outputURI
	 *            the output file URI.
	 */
	public void setOutputURI(final String outputURI) {
		setString(OUTPUT_URI, outputURI);
	}

	/**
	 * Sets the NIF serialization language.
	 * 
	 * @param nifLanguage
	 *            the NIF serialization language.
	 */
	public void setNifLanguage(final String nifLanguage) {
		setString(NIF_LANGUAGE, nifLanguage);
	}

	/**
	 * Gets the output base path.
	 * 
	 * @return the output base path.
	 */
	public String getOutBasePath() {
		return getString(OUTPUT_BASE_PATH);
	}

	/**
	 * Sets the output base path.
	 * 
	 * @param outputBasePath
	 *            the output base path.
	 */
	public void setOutBasePath(final String outputBasePath) {
		setString(OUTPUT_BASE_PATH, outputBasePath);
	}

	/**
	 * Gets the URI prefix to be used in the NIF resources definition.
	 * 
	 * @return the URI prefix to be used in the NIF resources definition.
	 */
	public String getNifURIPrefix() {
		return getString(NIF_URI_PREFIX);
	}

	/**
	 * Sets the URI prefix to be used in the NIF resources definition.
	 * 
	 * @param nifUriPrefix
	 *            the URI prefix to be used in the NIF resources definition.
	 */
	public void setNifURIPrefix(final String nifUriPrefix) {
		setString(NIF_URI_PREFIX, nifUriPrefix);
	}
}
