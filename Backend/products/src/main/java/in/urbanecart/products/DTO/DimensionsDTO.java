package in.urbanecart.products.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionsDTO {
    private Float width;
    private Float height;
    private Float depth;
}
