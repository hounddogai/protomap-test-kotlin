package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newTransitTimeBudget("code-916", "title-815")
        assertEquals(TransitTimeBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTransitTimeBudget("code-916", "title-815")
        try {
            item.moveTo(TransitTimeBudgetStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeBudgetStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTransitTimeBudget("code-916", "title-815")
        item.status = TransitTimeBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(TransitTimeBudgetStatus.DRAFT) }
    }
}
