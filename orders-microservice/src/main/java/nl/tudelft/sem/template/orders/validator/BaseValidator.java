package nl.tudelft.sem.template.orders.validator;

public abstract class BaseValidator {
    private Validator next;

    public void setNext(Validator v) {
        this.next = v;
    }

    protected boolean handle() throws ValidationFailureException {
        if (next == null) {
            return true;
        }

        return next.handle();
    }
}
