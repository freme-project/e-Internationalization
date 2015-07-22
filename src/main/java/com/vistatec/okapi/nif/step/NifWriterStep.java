package com.vistatec.okapi.nif.step;

import com.vistatec.okapi.nif.filter.NifWriterFilter;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;

public class NifWriterStep extends BasePipelineStep {

	private NifParameters params;

	private NifWriterFilter writer;
//	private ICodec codec;
	
	private LocaleId trgLoc;

	public NifWriterStep() {

		params = new NifParameters();
//		codec = OPCPackageReader.CODEC;
	}

	@Override
	public String getName() {

		return "NIF writer";
	}

	@Override
	public String getDescription() {
		return "Generate NIF file. Expects: filter events. Sends back: filter events.";
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
		if (writer != null) {
			writer.setParameters(params);
		}

	}

	@Override
	public Event handleEvent(Event event) {
		switch (event.getEventType()) {
		case NO_OP:
			return event;
		case START_DOCUMENT:
			processStartDocument(event.getStartDocument());
			break;

		case TEXT_UNIT:
			ITextUnit tu = event.getTextUnit();			
			Event ev = new Event(EventType.TEXT_UNIT, tu.clone());
			processTextUnit(tu); 
			return ev;
		case END_DOCUMENT:
			processEndDocument();
			break;
		case END_GROUP:
		case END_SUBFILTER:
			break;

		case CANCELED:
		case CUSTOM:
		case DOCUMENT_PART:
		case START_BATCH:
		case END_BATCH:
		case MULTI_EVENT:
		case PIPELINE_PARAMETERS:
		case RAW_DOCUMENT:
		case START_BATCH_ITEM:
		case END_BATCH_ITEM:
		case START_SUBDOCUMENT:
		case END_SUBDOCUMENT:
		case START_GROUP:
		case START_SUBFILTER:
			break;
		default:
			break;
		}
		return event;
	}

	private void processTextUnit(ITextUnit textUnit) {
		
		textUnit = textUnit.clone();
//		CodecUtil.encodeTextUnit(textUnit, codec);
		writer.processTextUnit(textUnit);
	}

	private void processEndDocument() {
		if (writer != null) {
			writer.processEndDocument();
			writer = null;
		}

	}

	private void processStartDocument(StartDocument startDocument) {
		writer = new NifWriterFilter(params, startDocument.getLocale(), trgLoc);
		writer.processStartDocument(startDocument);
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.trgLoc = targetLocale;
	}

	// TODO check if isDone has to be overridden --> the original always returns
	// true
	// @Override
	// public boolean isDone() {
	// // TODO Auto-generated method stub
	// return false;
	// }

	// TODO check if destroy has to be overridden --> the original does nothing
	// @Override
	// public void destroy() {
	// // TODO Auto-generated method stub
	//
	// }

	// TODO check if cancel has to be overridden --> the original does nothing
	// @Override
	// public void cancel() {
	// // TODO Auto-generated method stub
	//
	// }

	// @Override
	// public boolean isLastOutputStep() {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public void setLastOutputStep(boolean isLastStep) {
	// // TODO Auto-generated method stub
	//
	// }

}
