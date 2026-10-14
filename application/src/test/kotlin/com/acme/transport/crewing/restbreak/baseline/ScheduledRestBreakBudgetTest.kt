package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRestBreakBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledRestBreakBudget("code-897", "title-230")
        assertEquals(ScheduledRestBreakBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newScheduledRestBreakBudget("code-897", "title-230")
        try {
            item.moveTo(ScheduledRestBreakBudgetStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRestBreakBudgetStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledRestBreakBudget("code-897", "title-230")
        item.status = ScheduledRestBreakBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledRestBreakBudgetStatus.DRAFT) }
    }
}
