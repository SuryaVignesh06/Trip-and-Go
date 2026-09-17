package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.entity.Expense;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.BudgetRepository;
import com.tripnest.tripnest_backend.repository.ExpenseRepository;
import com.tripnest.tripnest_backend.repository.TripMemberRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final TripAccessService tripAccessService;
    private final NotificationService notificationService;
    private final TripMemberRepository tripMemberRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            TripRepository tripRepository,
            BudgetRepository budgetRepository,
            UserRepository userRepository,
            TripAccessService tripAccessService,
            NotificationService notificationService,
            TripMemberRepository tripMemberRepository) {

        this.expenseRepository = expenseRepository;
        this.tripRepository = tripRepository;
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.tripAccessService = tripAccessService;
        this.notificationService = notificationService;
        this.tripMemberRepository = tripMemberRepository;
    }

    // CREATE EXPENSE
    public Expense createExpense(
            Integer tripId,
            Expense expense) {

        validateExpense(expense);

        Trip trip =
                tripRepository.findById(tripId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Trip not found"));

        Budget budget =
                budgetRepository.findByTripId(tripId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Budget not found for this trip"));

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        expense.setTrip(trip);
        expense.setBudget(budget);
        expense.setPayer(currentUser);

        Expense savedExpense =
                expenseRepository.save(expense);

        // Check whether budget reached 80% or 100%
        checkBudgetThreshold(trip, budget);

        return savedExpense;
    }


    // LIST EXPENSES
    public List<Expense> getExpenses(
            Integer tripId) {

        tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Trip not found"));

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        return expenseRepository
                .findByTripId(tripId);
    }


    // UPDATE EXPENSE
    public Expense updateExpense(
            Integer expenseId,
            Expense updatedExpense) {

        validateExpense(updatedExpense);

        Expense existingExpense =
                expenseRepository.findById(expenseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense not found"));

        User currentUser = getCurrentUser();

        Trip trip =
                existingExpense.getTrip();

        tripAccessService.checkAccess(
                trip.getId(),
                currentUser);

        boolean isOwnerOrAdmin =
                tripAccessService.isOwnerOrGroupAdmin(
                        trip.getId(),
                        currentUser);

        boolean isPayer =
                existingExpense
                        .getPayer()
                        .getId()
                        .equals(currentUser.getId());

        if (!isOwnerOrAdmin && !isPayer) {

            throw new RuntimeException(
                    "You are not authorized to update this expense");
        }

        existingExpense.setCategory(
                updatedExpense.getCategory());

        existingExpense.setAmount(
                updatedExpense.getAmount());

        existingExpense.setDate(
                updatedExpense.getDate());

        existingExpense.setReceiptLink(
                updatedExpense.getReceiptLink());

        Expense savedExpense =
                expenseRepository.save(existingExpense);

        Budget budget =
                budgetRepository
                        .findByTripId(trip.getId())
                        .orElse(null);

        if (budget != null) {
            checkBudgetThreshold(
                    trip,
                    budget);
        }

        return savedExpense;
    }


    // DELETE EXPENSE
    public void deleteExpense(
            Integer expenseId) {

        Expense expense =
                expenseRepository.findById(expenseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense not found"));

        User currentUser = getCurrentUser();

        Trip trip =
                expense.getTrip();

        // User must belong to the trip
        tripAccessService.checkAccess(
                trip.getId(),
                currentUser);

        // Owner or Group Admin can delete any expense
        boolean isOwnerOrAdmin =
                tripAccessService.isOwnerOrGroupAdmin(
                        trip.getId(),
                        currentUser);

        // Regular member can delete only their own expense
        boolean isPayer =
                expense.getPayer()
                        .getId()
                        .equals(currentUser.getId());

        if (!isOwnerOrAdmin && !isPayer) {

            throw new RuntimeException(
                    "You are not authorized to delete this expense");
        }

        expenseRepository.delete(expense);
    }


    // CATEGORY SUMMARY
    public List<Object[]> getCategorySummary(
            Integer tripId) {

        tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Trip not found"));

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        return expenseRepository
                .getCategorySummary(tripId);
    }


    // REMAINING BUDGET
    public BigDecimal getRemainingBudget(
            Integer tripId) {

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        Budget budget =
                budgetRepository.findByTripId(tripId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Budget not found for this trip"));

        BigDecimal totalExpenses =
                expenseRepository
                        .getTotalExpensesByTripId(tripId);

        return budget.getAmount()
                .subtract(totalExpenses);
    }


    // VALIDATE EXPENSE
    private void validateExpense(
            Expense expense) {

        if (expense == null) {

            throw new RuntimeException(
                    "Expense data is required");
        }

        if (expense.getAmount() == null ||
                expense.getAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Expense amount cannot be negative");
        }

        if (expense.getCategory() == null ||
                expense.getCategory().isBlank()) {

            throw new RuntimeException(
                    "Category is required");
        }

        if (expense.getDate() == null) {

            throw new RuntimeException(
                    "Date is required");
        }
    }


    // CHECK BUDGET THRESHOLD
    private void checkBudgetThreshold(
            Trip trip,
            Budget budget) {

        BigDecimal totalExpenses =
                expenseRepository
                        .getTotalExpensesByTripId(
                                trip.getId());

        if (budget.getAmount() == null ||
                budget.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            return;
        }

        BigDecimal percentage =
                totalExpenses
                        .divide(
                                budget.getAmount(),
                                4,
                                RoundingMode.HALF_UP)
                        .multiply(
                                BigDecimal.valueOf(100));

        if (percentage.compareTo(
                BigDecimal.valueOf(100)) >= 0) {

            notifyBudgetThreshold(
                    trip,
                    "100%",
                    "Your trip budget has reached or exceeded 100%.");
        }

        else if (percentage.compareTo(
                BigDecimal.valueOf(80)) >= 0) {

            notifyBudgetThreshold(
                    trip,
                    "80%",
                    "Your trip budget has reached 80%.");
        }
    }


    // SEND BUDGET NOTIFICATION
    private void notifyBudgetThreshold(
            Trip trip,
            String threshold,
            String message) {

        String type =
                "BUDGET_ALERT_" + threshold;

        // Notify trip owner
        notificationService
                .createNotificationIfNotExists(
                        trip.getOwner(),
                        "Budget Alert",
                        message,
                        type);

        // Notify all trip members
        tripMemberRepository
                .findByTripId(trip.getId())
                .forEach(member ->
                        notificationService
                                .createNotificationIfNotExists(
                                        member.getUser(),
                                        "Budget Alert",
                                        message,
                                        type));
    }


    // CURRENT LOGGED-IN USER
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
                                "User not found"));
    }
}