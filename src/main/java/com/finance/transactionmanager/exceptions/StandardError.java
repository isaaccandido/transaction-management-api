package com.finance.transactionmanager.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;

import static com.finance.transactionmanager.configs.ApiConstants.DEFAULT_DATE_PATTERN;
import static com.finance.transactionmanager.configs.ApiConstants.DEFAULT_TIME_ZONE;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StandardError {
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DEFAULT_DATE_PATTERN,
            timezone = DEFAULT_TIME_ZONE
    )
    private Instant timestamp;
    private Integer status;
    private String error;
    private String path;
}