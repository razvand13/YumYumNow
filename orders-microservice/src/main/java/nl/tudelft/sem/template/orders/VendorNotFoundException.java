package nl.tudelft.sem.template.orders;

public class VendorNotFoundException extends RuntimeException {
    static final long serialVersionUID = -3387516993124229948L;

    public VendorNotFoundException(String message) {
        super(message);
    }

    public VendorNotFoundException() {
    }
}
