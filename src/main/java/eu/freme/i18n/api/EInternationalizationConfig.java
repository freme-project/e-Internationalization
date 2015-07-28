package eu.freme.i18n.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EInternationalizationConfig {

	@Bean
	public EInternationalizationAPI nifConverter(){
		return new EInternationalizationAPI();
	}
}
