package co.com.bancolombia.model.exceptions;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("El correo_electronico ya está registrado: " + email);
    }
}