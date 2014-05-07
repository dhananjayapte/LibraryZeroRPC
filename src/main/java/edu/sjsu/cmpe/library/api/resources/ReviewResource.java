package edu.sjsu.cmpe.library.api.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Review;
import edu.sjsu.cmpe.library.dto.BookDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.dto.ReviewDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

@Path("/v1/books/{isbn}/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewResource {
	/** bookRepository instance */
	private final BookRepositoryInterface bookRepository;

	/**
	 * BookResource constructor
	 * 
	 * @param bookRepository
	 *            a BookRepository instance
	 */
	public ReviewResource(BookRepositoryInterface bookRepository) {
		this.bookRepository = bookRepository;
	}

	@GET
	@Timed(name = "view-review")
	public Response viewAllReviews(@PathParam("isbn") Long isbn){
		Book reviewBook = bookRepository.getBookByISBN(isbn);
		List<Review> reviewList = null;
		if(reviewBook.getReviewList()!=null && !reviewBook.getReviewList().isEmpty()){
			reviewList = reviewBook.getReviewList();
		}
		Map<String, List<Review>> testMap = new HashMap<String, List<Review>>();
		testMap.put("reviews", reviewList);
		testMap.put("links", new ArrayList<Review>());
		return Response.status(200).entity(testMap).build();
	}
	
	@POST
	@Timed(name = "create-review")
	public Response createBookReview(@PathParam("isbn") LongParam isbn, Review request) {
		if(request.getComment()==null){
			return Response.status(400).type("text/plain").entity("Comment cannot be empty").build();
		}
		if(request.getRating() == 0){
			return Response.status(400).type("text/plain").entity("Rating cannot be empty").build();
		}
		
		final List<Integer> ratingList = Arrays.asList(1,2,3,4,5);
		
		if(!ratingList.contains(request.getRating())){
			return Response.status(400).type("text/plain")
					.entity("Invalid Rating. Rating must be between 1 to 5").build();
		}
		
		int reviewId = bookRepository.createBookReview(isbn.get(), request);
		Book reviewBook = bookRepository.getBookByISBN(isbn.get());
		
		BookDto bookResponse = new BookDto(reviewBook);
		bookResponse.addLink(new LinkDto("view-review", "/books/" + isbn
				+ "/reviews/" + reviewId, "GET"));

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("links", bookResponse.getLinks());

		return Response.status(201).entity(resultMap).build();
	}

	@GET
	@Path("/{id}")
	@Timed(name = "view-review")
	public Response getBookByReviewId(@PathParam("isbn") Long isbn, @PathParam("id") int reviewId) {
		Book reviewBook = bookRepository.getBookByISBN(isbn);

		List<Review> reviewList = reviewBook.getReviewList();
		Review reviewDetails = null;
		if (reviewList != null && !reviewList.isEmpty()) {
			for (Review reviewObj : reviewList) {
				if (reviewObj.getId() == reviewId) {
					reviewDetails = reviewObj;
				}
			}
		}

		ReviewDto bookResponse = new ReviewDto(reviewDetails);
		bookResponse.addLink(new LinkDto("view-review", "/books/" + reviewBook.getIsbn()
				+ "/reviews/" + reviewId, "GET"));
		return Response.status(200).entity(bookResponse).build();
	}

	/*private Map<String, Object> createDisplayMap(ReviewDto bookResponse) {
		Map<String, Object> displayMap = new HashMap<String, Object>();
		displayMap.put("links", bookResponse.getLinks());
		return displayMap;
	}
*/
}
