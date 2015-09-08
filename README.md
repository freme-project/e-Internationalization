# e-Internationalization
## Okapi-NIF Converter
This converter performs raw document to NIF conversion through Okapi.

##Building
```
mvn clean install
```

##Run Test
Run the junit test `NifWriterStepTest`.
This test creates and executes an Okapi pipeline. In the pipeline you can define which files to convert (XBatchItem) and which steps to perform. 

```
new XPipeline("Test pipeline for NifWriterStep", new XBatch(
				new XBatchItem(new File(src1Path, "test1.xlf").toURI().toURL(),
						"UTF-8", ENUS, ITIT),

				new XBatchItem(new File(src1Path, "test12.html").toURI()
						.toURL(), "UTF-8", ENUS, DEDE),

				new XBatchItem(new File(src1Path, "test8.txt").toURI().toURL(),
						"UTF-8", ENUS, DEDE)),

				new RawDocumentToFilterEventsStep(),

				new XPipelineStep(new NifWriterStep(), new XParameter(
						NifParameters.OUTPUT_BASE_PATH, pathBase),

						new XParameter(NifParameters.NIF_LANGUAGE,
								RDFSerialization.TURTLE.toRDFLang())))
				.execute();
```
The folder `okapi-nif-step\src\test\resources\nifConversion\src1` contains sample files in different formats. These files can be converted to NIF, by setting an appropriate XBatchItem into the pipeline. Files converted by the test are "test1.xlf", "test12.html" and "test8.txt". At the moment conversion from xliff, html and txt to NIF work fine. Other formats need to be tested.
If you want to convert your own files, be sure to put them into the src1 folder and to create the appopriate XBactchItem into the pipeline.

You can change the output files format, by setting the NIF_LANGUAGE parameter in the NifWriterStep. 
```
new XPipelineStep(new NifWriterStep(), new XParameter(
						NifParameters.OUTPUT_BASE_PATH, pathBase),

						new XParameter(NifParameters.NIF_LANGUAGE,
								RDFSerialization.TURTLE.toRDFLang())))
```
You can choose one among the following: Turtle (RDFSerialization.TURTLE.toRDFLang()), JSON (RDFSerialization.JSON-LD.toRDFLang()), RDF (null --> it is the default serialization format. Simply delete the `new XParameter(...)` parameter).

Converted files will be saved into the `okapi-nif-step\target\test-classes\nifConversion` folder.

## License

Copyright 2015 Deutsches Forschungszentrum für Künstliche Intelligenz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This project uses 3rd party tools. You can find the list of 3rd party tools including their authors and licenses [here](3RD-PARTY-LICENCES).





