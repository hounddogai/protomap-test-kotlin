package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNearMissBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingNearMissBudget("code-940", "title-323")
        assertEquals(PendingNearMissBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPendingNearMissBudget("code-940", "title-323")
        try {
            item.moveTo(PendingNearMissBudgetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNearMissBudgetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingNearMissBudget("code-940", "title-323")
        item.status = PendingNearMissBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(PendingNearMissBudgetStatus.DRAFT) }
    }
}
