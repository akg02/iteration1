package models;

import java.util.List;

/**
 * 
 * @author Joon Seung Hwang
 * @version 1
 *
 */
public class ProfileInfo {
	public String login;
	public String name;
	public String company;
	public String blog;
	public String location;
	public String email;
	public String bio;
	public String twitter_username;
	public int followers;
	public int following;
	public List<String> repos;
	
	public ProfileInfo() {
		
	}
	
	public ProfileInfo(String login, String name,	String company, String blog, String location, String email, String bio,
			String twitter_username, int followers, int following, List<String> repos) {
		this.login = login;
		this.name = name;
		this.company = company;
		this.blog = blog;
		this.location = location; 
		this.email = email;
		this.bio = bio;
		this.twitter_username = twitter_username;
		this.followers = followers; 
		this.following = following;
		this.repos = repos;
		
	}

}