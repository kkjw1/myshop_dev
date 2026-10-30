package myshop.shop.exception;

public class MemberMismatchException extends RuntimeException {
    public MemberMismatchException(String message) {
        super(message);
    }
}