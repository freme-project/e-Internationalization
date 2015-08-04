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

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;

public class NifParameters extends StringParameters {

	public static final String OUTPUT_URI = "outputURI";

	public static final String OUTPUT_BASE_PATH = "outBasePath";

	public static final String NIF_LANGUAGE = "nifLanguage";

	public static final String NIF_URI_PREFIX = "nifUriPrefix";

	@Override
	public void reset() {
		setOutputURI("");
		setNifLanguage("");
	}

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

	public String getOutputURI() {
		return getString(OUTPUT_URI);
	}

	public String getNifLanguage() {
		return getString(NIF_LANGUAGE);
	}

	public void setOutputURI(final String outputURI) {
		setString(OUTPUT_URI, outputURI);
	}

	public void setNifLanguage(final String nifLanguage) {
		setString(NIF_LANGUAGE, nifLanguage);
	}

	public String getOutBasePath() {
		return getString(OUTPUT_BASE_PATH);
	}

	public void setOutBasePath(final String outputBasePath) {
		setString(OUTPUT_BASE_PATH, outputBasePath);
	}
	
	public String getNifURIPrefix(){
		return getString(NIF_URI_PREFIX);
	}
	
	public void setNifURIPrefix(final String nifUriPrefix){
		setString(NIF_URI_PREFIX, nifUriPrefix);
	}
}
