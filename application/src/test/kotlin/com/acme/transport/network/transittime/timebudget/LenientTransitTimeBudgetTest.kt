package com.acme.transport.network.transittime.timebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTransitTimeBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientTransitTimeBudget("code-425", "title-186")
        assertEquals(LenientTransitTimeBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientTransitTimeBudget("code-425", "title-186")
        try {
            item.moveTo(LenientTransitTimeBudgetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTransitTimeBudgetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientTransitTimeBudget("code-425", "title-186")
        item.status = LenientTransitTimeBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(LenientTransitTimeBudgetStatus.DRAFT) }
    }
}
