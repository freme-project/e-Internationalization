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
package eu.freme.i18n.okapi.nif.filter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Helper class for markers management for the NIF skeleton writer filter.
 */
public class NifSkeletonMarkerHelper {

	/** The text enclosed into marker tag. */
	private String codedText;

	/** The list of codes contained in a text container. */
	private List<Code> codes;

	/** The current skeleton string. */
	private String skeleton;

	/**
	 * Manages codes contained in the current text fragment. Skeleton parts are
	 * put in the skeleton map, while text parts are stored into the text info
	 * list.
	 * 
	 * @param content
	 *            the text fragment content
	 * @param textUnitId
	 *            the text unit id.
	 * @param skeletonMap
	 *            the skeleton map
	 * @param textInfoList
	 *            the text info list
	 * @param skeletonParam
	 *            the skeleton for the current text unit.
	 */
	public void manageCodes(TextFragment content, String textUnitId,
			LinkedHashMap<String, String> skeletonMap,
			List<TextUnitInfo> textInfoList, String skeletonParam) {

		codedText = content.getCodedText();
		codes = content.getCodes();
		StringBuilder tmp = new StringBuilder();
		boolean startPointerFound = false;
		LinkedList<String> skeletonChunks = createSkeletonChunks(skeletonParam);
		this.skeleton = skeletonChunks.poll();
		int index;
		Code code;
		int idCounter = 0;
		for (int i = 0; i < codedText.length(); i++) {
			switch (codedText.codePointAt(i)) {
			case TextFragment.MARKER_OPENING:
				/*
				 * Marker opening found. If some plain text has been found
				 * before the marker, then the text is managed along with the
				 * skeleton. Else if no plain text so far, the code is managed
				 * along with the skeleton. At the end, the code is stored into
				 * the skeleton map. The tmp string builder is cleared.
				 */
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				System.out.println(code.toString());
				String skeletonString = code.toString();
				if (tmp.length() > 0) {
					TextUnitInfo tuInfo = new TextUnitInfo();
					tuInfo.setText(tmp.toString());
					tuInfo.setTuId(textUnitId + "-" + (++idCounter));
					manageTextUnitAndSkeleton(tuInfo, textInfoList,
							skeletonMap, skeletonChunks);
				} else {
					if (skeleton != null
							&& skeleton
									.contains(SkeletonConstants.REPLACE_STRING)) {
						skeleton = skeleton.replace(
								SkeletonConstants.REPLACE_STRING,
								code.toString());
						skeletonString = skeleton;
						skeleton = skeletonChunks.poll();
					}

				}
				skeletonMap.put(textUnitId + "-" + (++idCounter),
						skeletonString);
				tmp = new StringBuilder();
				break;
			case TextFragment.MARKER_CLOSING:
				/*
				 * Marker closing found. If some text plain has been found so
				 * far, then it is stored in the text info list. At the end, the
				 * code is put into the skeleton map. The tmp string builder is
				 * cleared.
				 */
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				System.out.println(code.toString());
				if (tmp.length() > 0) {
					TextUnitInfo tuInfo = new TextUnitInfo();
					tuInfo.setOffset(0);
					tuInfo.setText(tmp.toString());
					tuInfo.setTuId(textUnitId + "-" + (++idCounter));
					skeletonMap.put(tuInfo.getTuId(), tuInfo.getText());
					textInfoList.add(tuInfo);
				}

				skeletonMap.put(textUnitId + "-" + (++idCounter),
						code.toString());
				tmp = new StringBuilder();
				break;
			case TextFragment.MARKER_ISOLATED:
				/*
				 * Isolated marker found. If some plain text has been found
				 * before the marker, then the text is managed along with the
				 * skeleton. Else if no plain text so far, the code is managed
				 * along with the skeleton. At the end, the code is stored into
				 * the skeleton map. The tmp string builder is cleared.
				 */
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				System.out.println(code.toString());
				skeletonString = code.toString();
				if (tmp.length() > 0) {
					TextUnitInfo tuInfo = new TextUnitInfo();
					tuInfo.setText(tmp.toString());
					tuInfo.setTuId(textUnitId + "-" + (++idCounter));
					manageTextUnitAndSkeleton(tuInfo, textInfoList,
							skeletonMap, skeletonChunks);
				} else {
					if (skeleton != null
							&& skeleton
									.contains(SkeletonConstants.REPLACE_STRING)) {
						skeleton = skeleton.replace(
								SkeletonConstants.REPLACE_STRING,
								code.toString());
						skeletonString = skeleton;
						skeleton = skeletonChunks.poll();
					}
				}

				skeletonMap.put(textUnitId + "-" + (++idCounter),
						skeletonString);
				tmp = new StringBuilder();

				break;
			case '[':
				/*
				 * Check this character because it could be the first char in
				 * the POINTER_STRING_PREFIX ("[#$<id>]"). If it is the case,
				 * the text found so far is managed along with the skeleton and
				 * the tmp string builder is cleared. The "startPointerFound"
				 * variable is set to true.
				 */
				if (codedText.substring(i,
						i + SkeletonConstants.POINTER_STRING_PREFIX.length())
						.equals(SkeletonConstants.POINTER_STRING_PREFIX)) {
					startPointerFound = true;
					if (tmp.length() > 0) {
						TextUnitInfo tuInfo = new TextUnitInfo();
						tuInfo.setText(tmp.toString());
						tuInfo.setTuId(textUnitId + "-" + (++idCounter));
						manageTextUnitAndSkeleton(tuInfo, textInfoList,
								skeletonMap, skeletonChunks);
						tmp = new StringBuilder();

					}
				}
				tmp.append(codedText.charAt(i));
				break;
			case ']':
				/*
				 * Check this character because it could be the last char in the
				 * POINTER_STRING_PREFIX ("[#$<id>]"). If the
				 * "startPointerFound" variable is set to true, then this is the
				 * last char of the POINTER_STRING_PREFIX. Then the whole
				 * pointer string (the text contained into the tmp string
				 * builder) is put into the skeleton map. The tmp string builder
				 * is cleared.
				 */
				tmp.append(codedText.charAt(i));
				if (startPointerFound) {
					startPointerFound = false;
					if (skeleton != null
							&& skeleton
									.contains(SkeletonConstants.REPLACE_STRING)) {
						skeleton = skeleton.replace(
								SkeletonConstants.REPLACE_STRING,
								tmp.toString());
						skeletonMap.put(textUnitId + "-" + (++idCounter),
								skeleton);
						skeleton = skeletonChunks.poll();
					} else {
						skeletonMap.put(textUnitId + "-" + (++idCounter),
								tmp.toString());
					}
					tmp = new StringBuilder();
				}
				break;
			default:
				tmp.append(codedText.charAt(i));
				break;
			}
		}
		// manage the remaining text and skeleton
		if (tmp.length() > 0) {
			TextUnitInfo tuInfo = new TextUnitInfo();
			tuInfo.setText(tmp.toString());
			if (idCounter == 0) {
				tuInfo.setTuId(textUnitId);
			} else {
				tuInfo.setTuId(textUnitId + "-" + (++idCounter));
			}
			manageTextUnitAndSkeleton(tuInfo, textInfoList, skeletonMap,
					skeletonChunks);
			StringBuilder skelString = new StringBuilder();
			if (skeleton != null) {
				skelString.append(skeleton);
				if (!skeletonChunks.isEmpty()) {
					while (skeletonChunks.peek() != null) {
						skelString.append(skeletonChunks.poll());
					}
				}
				skeletonMap.put(textUnitId + "-" + (++idCounter),
						skelString.toString());
			}
		}
	}

	/**
	 * Manages a text unit taking into account the skeleton. If the skeleton
	 * contains the "REPLACE_STRING" ([#$$self$]), then that string is replaced
	 * with the current text in the skeleton and the latter is stored into the
	 * skeleton map. The text unit is stored into the text unit list.
	 * 
	 * @param tuInfo
	 *            the text unit info
	 * @param textInfoList
	 *            the text info list
	 * @param skeletonMap
	 *            the skeleton map
	 * @param skeletonChuncks
	 *            the list of skeleton chunks
	 */
	private void manageTextUnitAndSkeleton(final TextUnitInfo tuInfo,
			final List<TextUnitInfo> textInfoList,
			final LinkedHashMap<String, String> skeletonMap,
			LinkedList<String> skeletonChuncks) {

		if (skeleton != null
				&& skeleton.contains(SkeletonConstants.REPLACE_STRING)) {
			int offset = skeleton.indexOf(SkeletonConstants.REPLACE_STRING);
			tuInfo.setOffset(offset);
			skeleton = skeleton.replace(SkeletonConstants.REPLACE_STRING,
					tuInfo.getText());
			skeletonMap.put(tuInfo.getTuId(), skeleton);
			textInfoList.add(tuInfo);
			skeleton = skeletonChuncks.poll();
		} else {
			tuInfo.setOffset(0);
			skeletonMap.put(tuInfo.getTuId(), tuInfo.getText());
			textInfoList.add(tuInfo);
		}
	}

	/**
	 * Creates a list of skeleton chunks. Chunks are created by dividing the
	 * skeleton so that each chunk contains a REPLACE_STRING ([#$$self$]) but
	 * the last one.
	 * 
	 * @param skeletonParam
	 *            the whole skeleton
	 * @return the list of skeleton chunks.
	 */
	private LinkedList<String> createSkeletonChunks(String skeletonParam) {

		LinkedList<String> skeletonChunks = new LinkedList<String>();
		skeletonParam = skeletonParam.replace(
				SkeletonConstants.STANDOFF_STRING_PREFIX, "");
		while (skeletonParam.contains(SkeletonConstants.REPLACE_STRING)) {
			int firstChunckLastIdx = skeletonParam
					.indexOf(SkeletonConstants.REPLACE_STRING)
					+ SkeletonConstants.REPLACE_STRING.length();
			skeletonChunks.add(skeletonParam.substring(0, firstChunckLastIdx));
			skeletonParam = skeletonParam.substring(firstChunckLastIdx);
		}
		if (!skeletonParam.isEmpty()) {
			skeletonChunks.add(skeletonParam);
		}
		return skeletonChunks;
	}
}
