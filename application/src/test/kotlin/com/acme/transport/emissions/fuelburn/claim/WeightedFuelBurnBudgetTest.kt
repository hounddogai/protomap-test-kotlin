package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelBurnBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedFuelBurnBudget("code-888", "name-952")
        assertEquals(WeightedFuelBurnBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedFuelBurnBudget("code-888", "name-952")
        try {
            item.moveTo(WeightedFuelBurnBudgetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelBurnBudgetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedFuelBurnBudget("code-888", "name-952")
        item.status = WeightedFuelBurnBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedFuelBurnBudgetStatus.PLANNED) }
    }
}
