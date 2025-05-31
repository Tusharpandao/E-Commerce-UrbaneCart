package in.urbanecart.products.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaDTO {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String barcode;
    private String qrCode;
}
