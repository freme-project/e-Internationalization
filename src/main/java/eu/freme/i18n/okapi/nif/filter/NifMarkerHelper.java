package eu.freme.i18n.okapi.nif.filter;


import java.nio.charset.CharsetEncoder;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
//import net.sf.okapi.steps.xliffkit.codec.DummyEncoder;

public class NifMarkerHelper {

	private String codedText;
	private List<Code> codes;
	private CharsetEncoder chsEnc;
	
	public NifMarkerHelper() {
//		chsEnc = new DummyEncoder();
//		codedText = textContainer.getCodedText();
//		codes = textContainer.getCodes();
	}
	
	
	
//	public String toString (int quoteMode,
//			boolean escapeGT,
//			boolean codeOnlyMode,
//			boolean gMode,
//			boolean codeAttrs,
//			boolean includeIts,
//			LocaleId trgLocId)
//		{
	public String toString ( TextFragment content)
		{
			codedText = content.getCodedText();
			codes = content.getCodes();
			StringBuilder tmp = new StringBuilder();
			int index;
			Code code;
			
			for ( int i=0; i<codedText.length(); i++ ) {
				switch ( codedText.codePointAt(i) ) {
				case TextFragment.MARKER_OPENING:
					index = TextFragment.toIndex(codedText.charAt(++i));
					code = codes.get(index);
					System.out.println(code.toString());
//					if ( codeOnlyMode ) {
//						tmp.append(code.toString());
//					}
//					else {
//						// Output the code (if it's not a marker-only one)
//						if ( !code.hasOnlyAnnotation() ) {
//							if ( gMode ) {
//								insertCodeStart(tmp, TAG.g, code, codeAttrs);
//								tmp.append(">");
//							}
//							else {
//								insertCodeStart(tmp, TAG.bpt, code, codeAttrs); //TODO: escape unsupported chars
//								tmp.append(">");
//								tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
//								tmp.append("</bpt>");
//							}
//						}
//						// Then, if needed, output the marker element
//						// (Markers linked to original codes have the marker inside the spanned content) 
//						if ( code.hasAnnotation("protected") ) {
//							tmp.append("<mrk mtype=\"protected\">");
//						}
//						else if ( includeIts && code.hasAnnotation(GenericAnnotationType.GENERIC) ) {
//							tmp.append("<mrk");
//							outputITSAttributes(code.getGenericAnnotations(), quoteMode, escapeGT, tmp, true, true, trgLocId);
//							tmp.append(">");
//						}
//					}
					break;
				case TextFragment.MARKER_CLOSING:
					index = TextFragment.toIndex(codedText.charAt(++i));
					code = codes.get(index);
					System.out.println(code.toString());
//					if ( codeOnlyMode ) {
//						tmp.append(code.toString());
//					}
//					else {
//						// Close the marker, if needed
//						if ( includeIts && code.hasAnnotation(GenericAnnotationType.GENERIC) ) {
//							tmp.append("</mrk>");
//						}
//						else if ( code.hasAnnotation("protected") ) {
//							tmp.append("</mrk>");
//						}
//						// Then close the code
//						if ( !code.hasOnlyAnnotation() ) {
//							if ( gMode ) {
//								tmp.append("</g>");
//							}
//							else {
//								insertCodeStart(tmp, TAG.ept, code, codeAttrs); //TODO: escape unsupported chars
//								tmp.append(">");
//								tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
//								tmp.append("</ept>");
//							}
//						}
//					}
					break;
				case TextFragment.MARKER_ISOLATED:
					index = TextFragment.toIndex(codedText.charAt(++i));
					code = codes.get(index);
					System.out.println(code.toString());
//					if ( codeOnlyMode ) {
//						tmp.append(code.toString());
//					}
//					else {
//						if ( gMode ) {
//							if ( code.getTagType() == TagType.OPENING ) {
//								insertCodeStart(tmp, TAG.bx, code, codeAttrs);
//								tmp.append("/>");
//							}
//							else if ( code.getTagType() == TagType.CLOSING ) {
//								insertCodeStart(tmp, TAG.ex, code, codeAttrs);
//								tmp.append("/>");
//							}
//							else {
//								insertCodeStart(tmp, TAG.x, code, codeAttrs);
//								if ( includeIts && code.hasAnnotation(GenericAnnotationType.GENERIC) ) {
//									outputITSAttributes(code.getGenericAnnotations(), quoteMode, escapeGT, tmp, true, false, null);
//								}
//								tmp.append("/>");
//							}
//						}
//						else {
//							if ( code.getTagType() == TagType.OPENING ) {
//								insertCodeStart(tmp, TAG.it, code, codeAttrs); //TODO: escape unsupported chars
//								tmp.append(" pos=\"open\">");
//								tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
//								tmp.append("</it>");
//							}
//							else if ( code.getTagType() == TagType.CLOSING ) {
//								insertCodeStart(tmp, TAG.it, code, codeAttrs); //TODO: escape unsupported chars
//								tmp.append(" pos=\"close\">");
//								tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
//								tmp.append("</it>");
//							}
//							else {
//								insertCodeStart(tmp, TAG.ph, code, codeAttrs); //TODO: escape unsupported chars
//								if ( includeIts && code.hasAnnotation(GenericAnnotationType.GENERIC) ) {
//									outputITSAttributes(code.getGenericAnnotations(), quoteMode, escapeGT, tmp, true, false, trgLocId);
//								}
//								tmp.append(">");
//								tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
//								tmp.append("</ph>");
//							}
//						}
//					}
					break;
				case '>':
//					if ( escapeGT ) tmp.append("&gt;");
//					else {
//						if (( i > 0 ) && ( codedText.charAt(i-1) == ']' )) 
//							tmp.append("&gt;");
//						else
//							tmp.append('>');
//					}
					break;
				case '\r': // Not a line-break in the XML context, but a literal
					tmp.append("&#13;");
					break;
				case '<':
					tmp.append("&lt;");
					break;
				case '&':
					tmp.append("&amp;");
					break;
				case '"':
//					if ( quoteMode > 0 ) tmp.append("&quot;");
//					else tmp.append('"');
					break;
				case '\'':
//					switch ( quoteMode ) {
//					case 1:
//						tmp.append("&apos;");
//						break;
//					case 2:
//						tmp.append("&#39;");
//						break;
//					default:
//						tmp.append(codedText.charAt(i));
//						break;
//					}
					break;
				default:
					if ( codedText.charAt(i) > 127 ) { // Extended chars
						if ( Character.isHighSurrogate(codedText.charAt(i)) ) {
							int cp = codedText.codePointAt(i++);
							String buf = new String(Character.toChars(cp));
							if (( chsEnc != null ) && !chsEnc.canEncode(buf) ) {
								tmp.append(String.format("&#x%X;", cp));
							} else {
								tmp.append(buf);
							}
						}
						else {
							if (( chsEnc != null ) && !chsEnc.canEncode(codedText.charAt(i)) ) {
								tmp.append(String.format("&#x%04X;", codedText.codePointAt(i)));
							}
							else { // No encoder or char is supported
								tmp.append(codedText.charAt(i));
							}
						}
					}
					else { // ASCII chars
						tmp.append(codedText.charAt(i));
					}
					break;
				}
			}
			return tmp.toString();
		}
}

