package com.example.nhom_15_dulichtphcm;

import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.local.ExpenseCategory;
import com.example.nhom_15_dulichtphcm.data.local.ExplorerRank;
import com.example.nhom_15_dulichtphcm.data.local.TravelPassportManager;
import com.example.nhom_15_dulichtphcm.data.local.TripExpenseManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PassportAndExpenseTest {

    @Before
    public void setUp() {
        AuthManager.resetForTesting();
        TravelPassportManager.resetForTesting();
        TripExpenseManager.resetForTesting();
        AttractionRepository.resetForTesting();
    }

    @Test
    public void passportCheckIn_enforcesRbacAndRankProgression() {
        List<Attraction> all = AttractionRepository.getAttractions();

        // Guest cannot stamp passport
        ResultBox<Boolean> guestAttempt = TravelPassportManager.toggleCheckIn(all.get(0));
        assertFalse(guestAttempt.isSuccess());

        // Sign in as Member
        assertTrue(AuthManager.login("tourist", "user123").isSuccess());
        assertEquals(ExplorerRank.NEWCOMER, TravelPassportManager.currentRank());

        for (int i = 0; i < 3; i++) {
            assertTrue(TravelPassportManager.toggleCheckIn(all.get(i)).getOrNull());
        }
        assertEquals(ExplorerRank.CITY_WALKER, TravelPassportManager.currentRank());

        for (int i = 3; i < 7; i++) {
            assertTrue(TravelPassportManager.toggleCheckIn(all.get(i)).getOrNull());
        }
        assertEquals(ExplorerRank.HERITAGE_HUNTER, TravelPassportManager.currentRank());

        for (int i = 7; i < 12; i++) {
            assertTrue(TravelPassportManager.toggleCheckIn(all.get(i)).getOrNull());
        }
        assertEquals(ExplorerRank.SOUTHERN_AMBASSADOR, TravelPassportManager.currentRank());
    }

    @Test
    public void tripExpenseManager_computesRemainingBudgetAndCeilingSplit() {
        assertTrue(TripExpenseManager.setBudget(2_000_000L).isSuccess());
        assertTrue(TripExpenseManager.addExpense("Cơm tấm Ba Ghiền", 125_000L, ExpenseCategory.FOOD).isSuccess());
        assertTrue(TripExpenseManager.addExpense("Taxi Xanh SM", 175_001L, ExpenseCategory.TRANSPORT).isSuccess());

        assertEquals(300_001L, TripExpenseManager.getTotalSpentVnd());
        assertEquals(1_699_999L, TripExpenseManager.getRemainingBudgetVnd());
        // Ceiling split: 300,001 / 3 = 100,001 VND
        assertEquals(100_001L, TripExpenseManager.splitPerPerson(3));
    }
}
