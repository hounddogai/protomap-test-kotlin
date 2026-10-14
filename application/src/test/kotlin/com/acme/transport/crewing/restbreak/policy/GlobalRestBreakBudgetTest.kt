package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRestBreakBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalRestBreakBudget("code-164", "label-221")
        assertEquals(GlobalRestBreakBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newGlobalRestBreakBudget("code-164", "label-221")
        try {
            item.moveTo(GlobalRestBreakBudgetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRestBreakBudgetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalRestBreakBudget("code-164", "label-221")
        item.status = GlobalRestBreakBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalRestBreakBudgetStatus.OPEN) }
    }
}
