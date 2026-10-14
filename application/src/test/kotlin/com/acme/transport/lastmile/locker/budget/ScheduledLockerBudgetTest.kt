package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLockerBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledLockerBudget("code-345", "name-52")
        assertEquals(ScheduledLockerBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newScheduledLockerBudget("code-345", "name-52")
        try {
            item.moveTo(ScheduledLockerBudgetStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLockerBudgetStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledLockerBudget("code-345", "name-52")
        item.status = ScheduledLockerBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledLockerBudgetStatus.OPEN) }
    }
}
