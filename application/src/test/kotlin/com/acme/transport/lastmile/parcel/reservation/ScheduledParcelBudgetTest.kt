package com.acme.transport.lastmile.parcel.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledParcelBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledParcelBudget("code-600", "title-203")
        assertEquals(ScheduledParcelBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScheduledParcelBudget("code-600", "title-203")
        try {
            item.moveTo(ScheduledParcelBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledParcelBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledParcelBudget("code-600", "title-203")
        item.status = ScheduledParcelBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledParcelBudgetStatus.REQUESTED) }
    }
}
