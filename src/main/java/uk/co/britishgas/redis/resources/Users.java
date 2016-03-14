package uk.co.britishgas.redis.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

import uk.co.britishgas.redis.annotation.JsonParam;
import uk.co.britishgas.redis.models.User;

@Component
@Path("/users")
@Produces("application/json")
@Consumes("application/json")
public class Users {

	@POST
	public Response saveUser(@JsonParam("user") User user) {		
		return Response.ok(user).status(Status.CREATED).build();
	}
	
}
