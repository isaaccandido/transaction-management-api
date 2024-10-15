package com.finance.transactionmanager.models.generic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Schema(name = "Content")
public class CollectionContentWrapper<T> {
    private List<T> data;
    private int page;
    private int totalPages;
    private int size;
    private Long totalElements;
}
