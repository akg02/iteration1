package models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Issue of a repository
 * @author Meet Mehta
 *
 */
public class Issue implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("title")
	private String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Default Constructor
	 */
	public Issue() {
		
	}
	public Issue(String title) {
		this.title = title;
	}

}
