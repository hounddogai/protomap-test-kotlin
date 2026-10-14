package com.acme.transport.emissions.fuelburn.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFuelBurnBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientFuelBurnBudget("code-415", "name-429")
        assertEquals(LenientFuelBurnBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLenientFuelBurnBudget("code-415", "name-429")
        try {
            item.moveTo(LenientFuelBurnBudgetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFuelBurnBudgetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientFuelBurnBudget("code-415", "name-429")
        item.status = LenientFuelBurnBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientFuelBurnBudgetStatus.REQUESTED) }
    }
}
