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

	@JsonProperty("labels")
	private String labels;

	@JsonProperty("number")
	private String number;

	@JsonProperty("state")
	private String state;

	@JsonProperty("body")
	private String body;


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
	public Issue(String title, String labels, String number, String state, String body) {
		this.title = title;
		this.labels = labels;
		this.number = number;
		this.state = state;
		this.body = body;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
