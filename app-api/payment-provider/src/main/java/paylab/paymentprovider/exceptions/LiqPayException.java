package paylab.paymentprovider.exceptions;

public class LiqPayException extends RuntimeException {

    public LiqPayException(String message) {
        super(message);
    }

    public LiqPayException(String message, Throwable cause) {
        super(message, cause);
    }
}
