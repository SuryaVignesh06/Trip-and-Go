package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.TripAnalyticsResponse;
import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.ActivityRepository;
import com.tripnest.tripnest_backend.repository.BudgetRepository;
import com.tripnest.tripnest_backend.repository.ExpenseRepository;
import com.tripnest.tripnest_backend.repository.ItineraryRepository;
import com.tripnest.tripnest_backend.repository.TripMemberRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TripAccessService tripAccessService;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final ItineraryRepository itineraryRepository;
    private final ActivityRepository activityRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    public TripAnalyticsResponse getTripAnalytics(
            Integer tripId) {

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        Trip trip =
                tripAccessService.getTrip(tripId);

        Budget budget =
                budgetRepository
                        .findByTripId(tripId)
                        .orElse(null);

        BigDecimal budgetAmount =
                budget != null
                        ? budget.getAmount()
                        : BigDecimal.ZERO;

        BigDecimal totalExpenses =
                expenseRepository
                        .getTotalExpensesByTripId(tripId);

        BigDecimal remainingBudget =
                budgetAmount.subtract(totalExpenses);

        long memberCount =
                tripMemberRepository
                        .findByTripId(tripId)
                        .size() + 1L;

        long itineraryCount =
                itineraryRepository
                        .findByTripId(tripId)
                        .size();

        long activityCount =
                itineraryRepository
                        .findByTripId(tripId)
                        .stream()
                        .mapToLong(itinerary ->
                                activityRepository
                                        .findByItineraryId(
                                                itinerary.getId())
                                        .size())
                        .sum();

        List<TripAnalyticsResponse.CategoryExpense>
                categoryExpenses =
                expenseRepository
                        .getCategorySummary(tripId)
                        .stream()
                        .map(row ->
                                new TripAnalyticsResponse
                                        .CategoryExpense(
                                                (String) row[0],
                                                (BigDecimal) row[1]))
                        .toList();

        return new TripAnalyticsResponse(
                trip.getId(),
                trip.getTitle(),
                budgetAmount,
                totalExpenses,
                remainingBudget,
                memberCount,
                itineraryCount,
                activityCount,
                categoryExpenses);
    }

    private User getCurrentUser() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"));
    }
}