package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Class to save profile information of a github user  
 * @author Joon Seung Hwang
 * 
 */
public class ProfileInfo {
	@JsonProperty("login")
	private String login;
	@JsonProperty("name")
	private String name;
	@JsonProperty("company")
	private String company;
	@JsonProperty("blog")
	private String blog;
	@JsonProperty("location")
	private String location;
	@JsonProperty("email")
	private String email;
	@JsonProperty("bio")
	private String bio;
	@JsonProperty("twitter_username")
	private String twitter_username;
	@JsonProperty("followers")
	private int followers;
	@JsonProperty("following")
	private int following;
	
	private List<String> repos;
	
	/**
	 * Default constructor for json
	 */
	public ProfileInfo() {
		
	}
	/**
	 * @return login name
	 */
	public String getLogin() {
		return login;
	}
	
	/**
	 * @return name of the user
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return company name
	 */
	public String getCompany() {
		return company;
	}
	/**
	 * @return blog name
	 */
	public String getBlog() {
		return blog;
	}
	/**
	 * @return location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @return email address
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @return bio description of the user
	 */
	public String getBio() {
		return bio;
	}
	/**
	 * @return twitter name
	 */
	public String getTwitter() {
		return twitter_username;
	}
	/**
	 * @return number of followers
	 */
	public int getFollowers() {
		return followers;
	}
	/**
	 * @return number of following
	 */
	public int getFollowing() {
		return following;
	}
	/**
	 * @return list of Strings of repository names
	 */
	public List<String> getRepos() {
		return repos;
	}
	/**
	 * Setter to set the repos attribute
	 * @param repos list of string of repository names
	 */
	public void setRepos(List<String> repos) {
		this.repos = repos;
	}
	

}