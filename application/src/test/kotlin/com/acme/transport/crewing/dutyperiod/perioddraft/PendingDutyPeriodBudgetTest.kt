package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDutyPeriodBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingDutyPeriodBudget("code-871", "name-163")
        assertEquals(PendingDutyPeriodBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPendingDutyPeriodBudget("code-871", "name-163")
        try {
            item.moveTo(PendingDutyPeriodBudgetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDutyPeriodBudgetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingDutyPeriodBudget("code-871", "name-163")
        item.status = PendingDutyPeriodBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(PendingDutyPeriodBudgetStatus.DRAFT) }
    }
}
