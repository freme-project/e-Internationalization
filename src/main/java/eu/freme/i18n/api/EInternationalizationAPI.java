package eu.freme.i18n.api;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class EInternationalizationAPI {

	public Reader convertToTurtle(InputStream is){
		String nif = "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n"
				+ "@prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n"
				+ "<http://example.org/document/1#char=0,19>\n"
				+ " a nif:String , nif:Context , nif:RFC5147String ;\n"
				+ " nif:isString \"Welcome to e-Internationalization\"@en;\n"
				+ " nif:beginIndex \"0\"^^xsd:nonNegativeInteger;\n"
				+ " nif:endIndex \"19\"^^xsd:nonNegativeInteger;\n"
				+ " nif:sourceUrl <http://differentday.blogspot.com/2007_01_01_archive.html> .";
		return new StringReader(nif);
	}
}
