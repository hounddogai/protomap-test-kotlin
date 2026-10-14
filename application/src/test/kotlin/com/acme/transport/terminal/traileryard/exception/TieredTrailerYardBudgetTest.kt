package com.acme.transport.terminal.traileryard.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTrailerYardBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredTrailerYardBudget("code-599", "label-784")
        assertEquals(TieredTrailerYardBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTieredTrailerYardBudget("code-599", "label-784")
        try {
            item.moveTo(TieredTrailerYardBudgetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTrailerYardBudgetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredTrailerYardBudget("code-599", "label-784")
        item.status = TieredTrailerYardBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(TieredTrailerYardBudgetStatus.DRAFT) }
    }
}
