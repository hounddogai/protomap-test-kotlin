package com.acme.transport.crewing.availability.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newAvailabilityBudget("code-563", "name-220")
        assertEquals(AvailabilityBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAvailabilityBudget("code-563", "name-220")
        try {
            item.moveTo(AvailabilityBudgetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityBudgetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAvailabilityBudget("code-563", "name-220")
        item.status = AvailabilityBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(AvailabilityBudgetStatus.DRAFT) }
    }
}
