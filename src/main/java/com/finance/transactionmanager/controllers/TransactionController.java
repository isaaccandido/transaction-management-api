package com.finance.transactionmanager.controllers;

import com.finance.transactionmanager.models.generic.CollectionContentWrapper;
import com.finance.transactionmanager.models.request.TransactionRequestModel;
import com.finance.transactionmanager.models.response.ExchangeResponseModel;
import com.finance.transactionmanager.models.response.TransactionResponseModel;
import com.finance.transactionmanager.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@CrossOrigin("*")
@AllArgsConstructor
@Tag(name = "Transactions Controller", description = "Endpoints for managing financial transactions.")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(
            summary = "Refresh Cache",
            description = "Refreshes cached exchange data manually."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful Operation."),
            @ApiResponse(responseCode = "400", description = "Bad Request."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    @GetMapping("/refresh-cache")
    public ResponseEntity<Void> refreshCache() {
        transactionService.refreshCache();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Retrieve All Transactions",
            description = "Fetches a list of all transactions stored in the database."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful Operation."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    @GetMapping("/all")
    public ResponseEntity<CollectionContentWrapper<TransactionResponseModel>> getAll(
            @RequestParam(defaultValue = "0", required = false)
            @Parameter(name = "page",
                    description = "Page number starting at 0.",
                    example = "0")
            int page,
            @RequestParam(defaultValue = "10", required = false)
            @Parameter(name = "size",
                    description = "Page size starting at 1.",
                    example = "1")
            int size) {
        return new ResponseEntity<>(transactionService.getAll(page, size), HttpStatus.OK);
    }

    @Operation(
            summary = "Create a New Transaction",
            description = "Creates a new transaction with a specified value and an " +
                    "optional description (up to 50 characters)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created."),
            @ApiResponse(responseCode = "400", description = "Bad Request."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    @PostMapping("/create")
    public ResponseEntity<TransactionResponseModel> create(@RequestBody
                                                           @Parameter(name = "transactionId",
                                                                   description = "Unique identifier for the " +
                                                                           "transaction.",
                                                                   example = "e7c9f1cd-da4e-4647-9830-ba4450d6f9a1")
                                                           @Valid TransactionRequestModel request) {
        return new ResponseEntity<>(transactionService.create(request), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Convert Currency for Existing Transaction",
            description = "Converts an existing transaction's amount to a user-specified target currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created."),
            @ApiResponse(responseCode = "400", description = "Bad Request."),
            @ApiResponse(responseCode = "404", description = "Not Found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    @GetMapping("/exchange")
    public ResponseEntity<ExchangeResponseModel> exchange(@RequestParam
                                                          @Parameter(name = "transactionId",
                                                                  description = "Unique identifier for the " +
                                                                          "transaction.",
                                                                  example = "e7c9f1cd-da4e-4647-9830-ba4450d6f9a1")
                                                          @NotNull
                                                          UUID transactionId,
                                                          @RequestParam
                                                          @Parameter(name = "targetCurrency",
                                                                  description = "The currency to convert to, " +
                                                                          "formatted as 'Country-Currency'.",
                                                                  example = "Brazil-Real")
                                                          @NotNull
                                                          String targetCurrency) {
        return new ResponseEntity<>(transactionService.exchange(transactionId, targetCurrency), HttpStatus.OK);
    }
}
