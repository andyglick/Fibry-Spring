package eu.lucaventuri.fibry.spring;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(FibryProperties.class)
@Import(FibryController.class)
public class FibryConfiguration {
}
