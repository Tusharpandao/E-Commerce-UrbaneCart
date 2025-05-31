package in.urbanecart.products.DTO;


import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private int rating;
    private String comment;
    private LocalDateTime date;
    private String reviewerName;
    private String reviewerEmail;
}
