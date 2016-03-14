package uk.co.britishgas.redis.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.britishgas.redis.cache.CacheService;
import uk.co.britishgas.redis.models.Book;

@Component
@Path("/messages")
@Produces("application/vnd.api+json")
public class Messages {

	@Autowired
	private CacheService cacheService;

	@GET
	public Response greeting(@QueryParam("id") String id) {
		Book book = cacheService.listMessages(id);
		return Response.ok(book).build();
	}

	@POST
	public Response saveGreeting(@QueryParam("id") String id, @QueryParam("name") String name) {
		Book book = new Book();
		book.setId(id);
		book.setName(name);
		cacheService.addMessage(book);
		return Response.ok("OK").status(Status.CREATED).build();
	}
}
