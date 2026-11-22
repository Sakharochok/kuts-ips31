package app.coffee.service;

/** Помилка читання або розбору файлу з початковими даними. */
public class DataLoadException extends RuntimeException {

    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
