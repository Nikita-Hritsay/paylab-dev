package paylab.paymentprovider.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class LiqPayException extends RuntimeException {

    public LiqPayException(String message) {
        super(message);
    }

    public LiqPayException(String message, Throwable cause) {
        super(message, cause);
    }
}
