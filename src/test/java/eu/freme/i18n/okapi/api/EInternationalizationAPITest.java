package eu.freme.i18n.okapi.api;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.freme.i18n.api.EInternationalizationAPI;
import eu.freme.i18n.api.EInternationalizationConfig;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EInternationalizationConfig.class)
public class EInternationalizationAPITest {

	@Autowired
	EInternationalizationAPI eInternationalizationAPI;
	
	@Test
	public void testEInternationalizationAPI(){
		
		InputStream is = null;
		String nif = eInternationalizationAPI.convertToTurtle(is).toString();
		assertTrue(nif.length()>0);
	}
}
