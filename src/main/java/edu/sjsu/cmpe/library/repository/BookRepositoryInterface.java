package edu.sjsu.cmpe.library.repository;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Review;

/**
 * Book repository interface.
 * 
 * What is repository pattern?
 * 
 * @see http://martinfowler.com/eaaCatalog/repository.html
 */
public interface BookRepositoryInterface {
    /**
     * Save a new book in the repository
     * 
     * @param newBook
     *            a book instance to be create in the repository
     * @return a newly created book instance with auto-generated ISBN
     */
    Book saveBook(Book newBook);

    /**
     * Retrieve an existing book by ISBN
     * 
     * @param isbn
     *            a valid ISBN
     * @return a book instance
     */
    Book getBookByISBN(Long isbn);

    // TODO: add other operations here!
    
    /**
     * This method will delete the book with given isbn
     * @param isbn
     */
    void deleteBook(Long isbn);
    
    /**
     * This method will update the book with given isbn
     * @param isbn
     * @param status 
     * @return
     */
    Book updateBook(Long isbn, String status);

	int createBookReview(Long isbn, Review reviewObj);

	Book getReviewById(Long isbn, int reviewId);
	
	public void setDateInMemoryMap(Long isbn, Date newDate);
	
	public ConcurrentHashMap<Long, Date> getDateInMemoryMap();
}
