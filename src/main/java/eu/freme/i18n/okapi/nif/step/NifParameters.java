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
