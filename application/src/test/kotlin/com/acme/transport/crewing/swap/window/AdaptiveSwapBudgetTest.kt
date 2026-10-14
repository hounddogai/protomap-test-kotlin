package com.acme.transport.crewing.swap.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveSwapBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveSwapBudget("code-167", "title-858")
        assertEquals(AdaptiveSwapBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveSwapBudget("code-167", "title-858")
        try {
            item.moveTo(AdaptiveSwapBudgetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveSwapBudgetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveSwapBudget("code-167", "title-858")
        item.status = AdaptiveSwapBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveSwapBudgetStatus.OPEN) }
    }
}
