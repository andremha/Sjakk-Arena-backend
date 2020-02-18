package no.ntnu.sjakkarena.exceptions;

/**
 * Exception thrown when data is not properly formed.
 */
public class ImproperlyFormedDataException extends RuntimeException{
    public ImproperlyFormedDataException() {
    }

    public ImproperlyFormedDataException(String message) {
        super(message);
    }
}