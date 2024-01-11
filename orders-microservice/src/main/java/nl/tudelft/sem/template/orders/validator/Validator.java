package nl.tudelft.sem.template.orders.validator;

public interface Validator {
    void setNext(Validator handler);

    boolean handle() throws ValidationFailureException;
}
