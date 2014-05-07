package edu.sjsu.cmpe.library.api.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Author;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.dto.AuthorDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

@Path("/v1/books/{isbn}/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorResource {
	/** bookRepository instance */
	private final BookRepositoryInterface bookRepository;

	/**
	 * BookResource constructor
	 * 
	 * @param bookRepository
	 *            a BookRepository instance
	 */
	public AuthorResource(BookRepositoryInterface bookRepository) {
		this.bookRepository = bookRepository;
	}
	
	@GET
	@Timed(name = "view-authors")
	public Response viewAllAuthors(@PathParam("isbn") Long isbn){
		Book authorBook = bookRepository.getBookByISBN(isbn);

		List<Author> authorList = null;
		if(authorBook.getAuthorList()!=null && !authorBook.getAuthorList().isEmpty()){
			authorList = authorBook.getAuthorList();
		}
		
		Map<String, List<Author>> displayAuthorMap = new HashMap<String, List<Author>>();
		displayAuthorMap.put("authors", authorList);
		displayAuthorMap.put("links", new ArrayList<Author>());
		return Response.status(200).entity(displayAuthorMap).build();
	}
	
	@GET
	@Path("/{id}")
	@Timed(name = "view-author")
	public Response viewAuthorById(@PathParam("isbn") Long isbn, @PathParam("id") int authorId){
		Book author = bookRepository.getBookByISBN(isbn);

		List<Author> authorList = author.getAuthorList();
		Author authorDetails = null;
		if (authorList != null && !authorList.isEmpty()) {
			for (Author authorObj : authorList) {
				if (authorObj.getId() == authorId) {
					authorDetails = authorObj;
				}
			}
		}

		AuthorDto bookResponse = new AuthorDto(authorDetails);
		bookResponse.addLink(new LinkDto("view-author", "/books/" + author.getIsbn() +
				"/authors/" + authorId, "GET"));
		return Response.status(200).entity(bookResponse).build();
	}
}
