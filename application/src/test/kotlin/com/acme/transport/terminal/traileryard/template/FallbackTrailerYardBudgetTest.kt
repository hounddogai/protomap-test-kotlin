package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTrailerYardBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackTrailerYardBudget("code-195", "name-615")
        assertEquals(FallbackTrailerYardBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newFallbackTrailerYardBudget("code-195", "name-615")
        try {
            item.moveTo(FallbackTrailerYardBudgetStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTrailerYardBudgetStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackTrailerYardBudget("code-195", "name-615")
        item.status = FallbackTrailerYardBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackTrailerYardBudgetStatus.PLANNED) }
    }
}
