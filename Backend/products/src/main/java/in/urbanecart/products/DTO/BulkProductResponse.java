package in.urbanecart.products.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkProductResponse {
    private List<ProductDto> added;
    private List<ProductDto> failed;
}

