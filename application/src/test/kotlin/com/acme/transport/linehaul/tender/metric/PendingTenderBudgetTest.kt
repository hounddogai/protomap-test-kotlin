package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTenderBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingTenderBudget("code-697", "title-871")
        assertEquals(PendingTenderBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingTenderBudget("code-697", "title-871")
        try {
            item.moveTo(PendingTenderBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTenderBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingTenderBudget("code-697", "title-871")
        item.status = PendingTenderBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingTenderBudgetStatus.REQUESTED) }
    }
}
