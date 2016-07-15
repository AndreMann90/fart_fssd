package de.fssd.parser;

/**
 * Used in {@link de.fssd.model.BDDBuilder}
 */
public class ParseException extends RuntimeException {

    public ParseException(String message) {
        super(message);
    }
}
