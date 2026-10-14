package com.acme.transport.emissions.fuelburn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newFuelBurnBudget("code-452", "title-507")
        assertEquals(FuelBurnBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFuelBurnBudget("code-452", "title-507")
        try {
            item.moveTo(FuelBurnBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFuelBurnBudget("code-452", "title-507")
        item.status = FuelBurnBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(FuelBurnBudgetStatus.REQUESTED) }
    }
}
