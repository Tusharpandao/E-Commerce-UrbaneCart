package in.urbanecart.products.exception;

public class ProductNotFound extends RuntimeException {

    public ProductNotFound(String massage) {
        super(massage);
    }
}
