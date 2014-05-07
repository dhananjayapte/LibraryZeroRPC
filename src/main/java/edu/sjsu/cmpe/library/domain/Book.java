package edu.sjsu.cmpe.library.domain;

import java.util.List;

import org.msgpack.annotation.Message;

import com.fasterxml.jackson.annotation.JsonProperty;

@Message
public class Book {
    private long isbn;
    private String title;
    

    // add more fields here
    private String language;
    private String publication_date;
    private int num_pages;
    private String status;
    private List<Author> authorList;
    private List<Review> reviewList;
  
    /**
     * @return the isbn
     */
    public long getIsbn() {
	return isbn;
    }

    /**
     * @param isbn
     *            the isbn to set
     */
    public void setIsbn(long isbn) {
	this.isbn = isbn;
    }

    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@JsonProperty("publication-date")
	public String getPublication_date() {
		return publication_date;
	}

	@JsonProperty("publication-date")
	public void setPublication_date(String publication_date) {
		this.publication_date = publication_date;
	}

	@JsonProperty("num-pages")
	public int getNum_pages() {
		return num_pages;
	}

	@JsonProperty("num-pages")
	public void setNum_pages(int num_pages) {
		this.num_pages = num_pages;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("authors")
	public List<Author> getAuthorList() {
		return authorList;
	}

	@JsonProperty("authors")
	public void setAuthorList(List<Author> authorList) {
		this.authorList = authorList;
	}

	@JsonProperty("reviews")
	public List<Review> getReviewList() {
		return reviewList;
	}

	@JsonProperty("reviews")
	public void setReviewList(List<Review> reviewList) {
		this.reviewList = reviewList;
	}	    
	
}
