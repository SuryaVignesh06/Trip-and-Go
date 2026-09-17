package com.tripnest.tripnest_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record TripAnalyticsResponse(

        Integer tripId,

        String tripTitle,

        BigDecimal budget,

        BigDecimal totalExpenses,

        BigDecimal remainingBudget,

        long memberCount,

        long itineraryCount,

        long activityCount,

        List<CategoryExpense> expensesByCategory
) {

    public record CategoryExpense(
            String category,
            BigDecimal amount
    ) {
    }
}