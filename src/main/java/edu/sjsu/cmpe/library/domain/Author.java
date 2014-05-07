package edu.sjsu.cmpe.library.domain;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.msgpack.annotation.Message;

import com.fasterxml.jackson.annotation.JsonProperty;

@Message
public class Author {
	private int id;
	@NotNull
	@NotEmpty
	private String name;

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
