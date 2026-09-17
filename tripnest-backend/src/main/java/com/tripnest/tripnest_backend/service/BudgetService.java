package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.BudgetRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TripAccessService tripAccessService;
    private final UserRepository userRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            TripAccessService tripAccessService,
            UserRepository userRepository) {

        this.budgetRepository = budgetRepository;
        this.tripAccessService = tripAccessService;
        this.userRepository = userRepository;
    }

    public Budget createBudget(
            Integer tripId,
            Budget budget) {

        validateBudget(budget);

        User currentUser = getCurrentUser();

        Trip trip = tripAccessService.getTrip(tripId);

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        if (budgetRepository.findByTripId(tripId).isPresent()) {
            throw new RuntimeException(
                    "Budget already exists for this trip");
        }

        budget.setTrip(trip);

        return budgetRepository.save(budget);
    }

    public Budget getBudgetByTripId(
            Integer tripId) {

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        return budgetRepository.findByTripId(tripId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Budget not found for this trip"));
    }

    public Budget updateBudget(
            Integer tripId,
            Budget updatedBudget) {

        validateBudget(updatedBudget);

        User currentUser = getCurrentUser();

        tripAccessService.checkOwnerOrGroupAdmin(
                tripId,
                currentUser);

        Budget existingBudget =
                budgetRepository.findByTripId(tripId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Budget not found for this trip"));

        existingBudget.setAmount(
                updatedBudget.getAmount());

        existingBudget.setCurrency(
                updatedBudget.getCurrency());

        return budgetRepository.save(existingBudget);
    }

    private void validateBudget(Budget budget) {

        if (budget == null) {
            throw new RuntimeException(
                    "Budget data is required");
        }

        if (budget.getAmount() == null ||
                budget.getAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Budget amount cannot be negative");
        }

        if (budget.getCurrency() == null ||
                budget.getCurrency().isBlank()) {

            throw new RuntimeException(
                    "Currency is required");
        }
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));
    }
}