package com.acme.transport.crewing.swap.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newSwapBudget("code-33", "name-666")
        assertEquals(SwapBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newSwapBudget("code-33", "name-666")
        try {
            item.moveTo(SwapBudgetStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapBudgetStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSwapBudget("code-33", "name-666")
        item.status = SwapBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(SwapBudgetStatus.OPEN) }
    }
}
