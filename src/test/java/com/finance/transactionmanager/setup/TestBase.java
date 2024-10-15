package com.finance.transactionmanager.setup;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public abstract class TestBase {
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
