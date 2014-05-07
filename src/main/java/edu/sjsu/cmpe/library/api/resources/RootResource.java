package edu.sjsu.cmpe.library.api.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.dto.LinksDto;

@Path("/v1/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RootResource {

    public RootResource() {
	// do nothing
    }

    @GET
    @Timed(name = "get-root")
    public Response getRoot() {
	LinksDto links = new LinksDto();
	links.addLink(new LinkDto("create-book", "/books", "POST"));

	return Response.ok(links).build();
    }
}

