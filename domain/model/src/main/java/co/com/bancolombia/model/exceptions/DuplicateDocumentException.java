package co.com.bancolombia.model.exceptions;

public class DuplicateDocumentException extends RuntimeException {
    public DuplicateDocumentException(String message) {

        super("El numero de documento ya está registrado: " + message);
    }
}
