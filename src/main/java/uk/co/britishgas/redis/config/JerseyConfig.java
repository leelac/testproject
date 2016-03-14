package uk.co.britishgas.redis.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import uk.co.britishgas.redis.provider.GsonProvider;
import uk.co.britishgas.redis.resources.Messages;
import uk.co.britishgas.redis.resources.Users;

@Configuration
public class JerseyConfig extends ResourceConfig {

	/**
	 * Constructor to initialize ResourceConfig for Jersey to use
	 */
	public JerseyConfig() {
		register(GsonProvider.class);
		register(Messages.class);
		register(Users.class);		
	}

}
