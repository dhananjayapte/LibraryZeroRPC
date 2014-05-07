package edu.sjsu.cmpe.library.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.sjsu.cmpe.library.domain.Author;
import edu.sjsu.cmpe.library.dto.LinksDto;

@JsonPropertyOrder(alphabetic = true)
public class AuthorDto extends LinksDto{
	private Author author;
	
	public AuthorDto(){}
	
	public AuthorDto(Author author){
		super();
		this.author = author;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

}
