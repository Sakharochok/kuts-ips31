package com.dance.parser;

/**
 * Помилка розбору XML-документа з танцями.
 */
public class DanceParseException extends RuntimeException {
    public DanceParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
