package uk.co.britishgas.redis.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Book implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;

	private String name;

	private String publisher;

	private String dateOfPublication;

	private String description;

	private String photo;

}
