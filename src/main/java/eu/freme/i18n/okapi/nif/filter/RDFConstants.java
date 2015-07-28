package eu.freme.i18n.okapi.nif.filter;

public class RDFConstants {

	public static final String nifPrefix = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
	public static final String itsrdfPrefix = "http://www.w3.org/2005/11/its/rdf#";
	public static final String xsdPrefix = "http://www.w3.org/2001/XMLSchema#";
	public static final String typePrefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public enum RDFSerialization {
		TURTLE("TTL"), JSON_LD("JSON-LD"), PLAINTEXT(null);
		
		private String rdfLang;
		
		private RDFSerialization(String rdfLang) {
			this.rdfLang = rdfLang;
		}
		public String toRDFLang(){
			
			return rdfLang;
		}
		
	}
}

