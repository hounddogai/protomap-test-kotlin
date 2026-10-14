package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newCapacityBudget("code-642", "title-73")
        assertEquals(CapacityBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCapacityBudget("code-642", "title-73")
        try {
            item.moveTo(CapacityBudgetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityBudgetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCapacityBudget("code-642", "title-73")
        item.status = CapacityBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(CapacityBudgetStatus.OPEN) }
    }
}
