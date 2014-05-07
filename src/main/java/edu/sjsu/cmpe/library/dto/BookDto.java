package edu.sjsu.cmpe.library.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.dto.LinksDto;


@JsonPropertyOrder(alphabetic = true)
public class BookDto extends LinksDto {
    private Book book;

    /**
     * @param book
     */
    public BookDto(Book book) {
	super();
	this.book = book;
    }

    public BookDto() {
		// TODO Auto-generated constructor stub
	}

	/**
     * @return the book
     */
    public Book getBook() {
	return book;
    }

    /**
     * @param book
     *            the book to set
     */
    public void setBook(Book book) {
	this.book = book;
    }
}
