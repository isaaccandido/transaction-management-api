package com.finance.transactionmanager.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Meta {
    @JsonProperty("count")
    private long pageCount;

    @JsonProperty("total-count")
    private long totalCount;

    @JsonProperty("total-pages")
    private long totalPages;
}
