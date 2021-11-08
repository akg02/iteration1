package controllers;

import play.data.validation.Constraints;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements search box.
 *
 * Holds an input text for the search box
 */
public class SearchForm {
    @Constraints.Required
    private String input;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}
