package chris;

/**
 * Represents an input error that Chris can explain to the user.
 */
public class ChrisException extends Exception {
    /** Version identifier used when this exception is serialized. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a user-friendly explanation.
     *
     * @param message explanation shown to the user
     */
    public ChrisException(String message) {
        super(message);
    }
}
