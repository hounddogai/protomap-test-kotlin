package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingLanePlanBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newPendingLanePlanBudget("code-793", "name-959")
        assertEquals(PendingLanePlanBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPendingLanePlanBudget("code-793", "name-959")
        try {
            item.moveTo(PendingLanePlanBudgetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingLanePlanBudgetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingLanePlanBudget("code-793", "name-959")
        item.status = PendingLanePlanBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingLanePlanBudgetStatus.OPEN) }
    }
}
