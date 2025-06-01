package in.urbanecart.products.exception;

public class CategoryNotFound extends  RuntimeException{

    public CategoryNotFound(String message) {
        super(message);
    }
}
