package com.finance.transactionmanager.setup.fixtures;

import com.finance.transactionmanager.rest.response.Meta;

public class MetaFixtures {
    private static final int PAGE_COUNT = 1;
    private static final int TOTAL_COUNT = 1;
    private static final int TOTAL_PAGES = 1;

    public static Meta getMeta() {
        return Meta.builder()
                .pageCount(PAGE_COUNT)
                .totalCount(TOTAL_COUNT)
                .totalPages(TOTAL_PAGES)
                .build();
    }
}
