package nl.tudelft.sem.template.orders.validator;

import org.springframework.http.HttpStatus;

public class ValidationFailureException extends RuntimeException {
    private final HttpStatus failureStatus;

    static final long serialVersionUID = -3387516993124229948L;

    /**
     * Construct a ValidationFailureException with default status
     */
    public ValidationFailureException() {
        super();
        //Default to internal server error -- in practice, this constructor should not be used
        this.failureStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Construct a ValidationFailureException with specified status
     *
     * @param failureStatus HttpStatus of the failure
     */
    public ValidationFailureException(HttpStatus failureStatus) {
        super();
        this.failureStatus = failureStatus;
    }

    public HttpStatus getFailureStatus() {
        return failureStatus;
    }
}
