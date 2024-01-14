package nl.tudelft.sem.template.orders.validator;

public abstract class BaseValidator implements Validator {
    private transient Validator next;

    public void setNext(Validator v) {
        this.next = v;
    }

    protected boolean checkNext(ValidatorRequest request) throws ValidationFailureException {
        if (next == null) {
            return true;
        }

        return next.handle(request);
    }
}
