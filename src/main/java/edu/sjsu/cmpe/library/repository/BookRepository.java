package edu.sjsu.cmpe.library.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import edu.sjsu.cmpe.library.domain.Author;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Review;

public class BookRepository implements BookRepositoryInterface {
    /** In-memory map to store books. (Key, Value) -> (ISBN, Book) */
    private final ConcurrentHashMap<Long, Book> bookInMemoryMap;
    private ConcurrentHashMap<Long, Date> dateInMemoryMap;
    
	/** Never access this key directly; instead use generateISBNKey() */
    private long isbnKey;
    private int authorId;
    private int reviewId;

    public BookRepository(ConcurrentHashMap<Long, Book> bookMap) {
	checkNotNull(bookMap, "bookMap must not be null for BookRepository");
	bookInMemoryMap = bookMap;
	isbnKey = 0;
	authorId = 0;
	reviewId = 0;
    }
    
    public void setDate(ConcurrentHashMap<Long, Date> dateMap){
    	dateInMemoryMap = dateMap;
    }

    /**
     * This should be called if and only if you are adding new books to the
     * repository.
     * 
     * @return a new incremental ISBN number
     */
    private final Long generateISBNKey() {
	// increment existing isbnKey and return the new value
	return Long.valueOf(++isbnKey);
    }
    
    /**
     * This method will generate a Author Id for each Author
     * @return
     */
    private final int generateAuthorId(){
		return  ++authorId;
    }
    
    private final int generateReviewId(){
		return  ++reviewId;
    }

    /**
     * This will auto-generate unique ISBN for new books.
     */
    @Override
    public Book saveBook(Book newBook) {
	checkNotNull(newBook, "newBook instance must not be null");
	// Generate new ISBN
	Long isbn = generateISBNKey();
	newBook.setIsbn(isbn);
	// TODO: create and associate other fields such as author
	List<Author> authorList = newBook.getAuthorList();
	if(authorList!=null && !authorList.isEmpty()){
		for(Author authObj : authorList){
			authObj.setId(generateAuthorId());
		}
		newBook.setAuthorList(authorList);
	}
	
	// Finally, save the new book into the map
	bookInMemoryMap.putIfAbsent(isbn, newBook);

	return newBook;
    }

    /**
     * @see edu.sjsu.cmpe.library.repository.BookRepositoryInterface#getBookByISBN(java.lang.Long)
     */
    @Override
    public Book getBookByISBN(Long isbn) {
	checkArgument(isbn > 0,
		"ISBN was %s but expected greater than zero value", isbn);
	return bookInMemoryMap.get(isbn);
    }

	@Override
	public void deleteBook(Long isbn) {
		bookInMemoryMap.remove(isbn);
	}

	@Override
	public Book updateBook(Long isbn, String status) {
		Book updateBook = getBookByISBN(isbn);
		updateBook.setStatus(status);
		bookInMemoryMap.put(isbn, updateBook);
		
		return updateBook;
	}

	@Override
	public int createBookReview(Long isbn, Review reviewObj) {
		Book reviewBook = getBookByISBN(isbn);
		reviewObj.setId(generateReviewId());
		List<Review> reviewList = null;
		if(reviewBook!=null){
			reviewList = reviewBook.getReviewList();
			if(reviewList==null || reviewList.isEmpty()){
				reviewList = new ArrayList<Review>();
			}
			reviewList.add(reviewObj);
		}
		reviewBook.setReviewList(reviewList);
		
		//old code
		/*List<Review> reviewList = new ArrayList<Review>();
		reviewList.add(reviewObj);
		int reviewId = generateReviewId();
		if(reviewList!=null && !reviewList.isEmpty()){
			for(Review reviewObj : reviewList){
				reviewObj.setId(generateReviewId());
			}
			reviewBook.setReviews(reviewList);
		}
		reviewBook.setReviews(reviewList);*/
		
		return reviewObj.getId();
	}

	/**
	 * This method will retrieve the reviews based on the Review ID and Book ISBN
	 */
	@Override
	public Book getReviewById(Long isbn, int reviewId) {
		Book reviewBook = getBookByISBN(isbn);
		List<Review> reviewList = reviewBook.getReviewList();
		if(reviewList!=null && !reviewList.isEmpty()){
			for(Review reviewObj : reviewList){
				if(reviewObj.getId()==reviewId){
					
				}
			}
		}
		return reviewBook;
	}
	
	public ConcurrentHashMap<Long, Date> getDateInMemoryMap() {
		return dateInMemoryMap;
	}

	public void setDateInMemoryMap(Long isbn, Date newDate) {
		ConcurrentHashMap<Long, Date> dateInMemoryMap = new ConcurrentHashMap<Long, Date>();
		dateInMemoryMap.put(isbn, newDate);
		this.dateInMemoryMap = dateInMemoryMap;
	}

}
