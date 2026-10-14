package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDutyPeriodBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingDutyPeriodBudget("code-645", "title-347")
        assertEquals(RollingDutyPeriodBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingDutyPeriodBudget("code-645", "title-347")
        try {
            item.moveTo(RollingDutyPeriodBudgetStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDutyPeriodBudgetStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingDutyPeriodBudget("code-645", "title-347")
        item.status = RollingDutyPeriodBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(RollingDutyPeriodBudgetStatus.DRAFT) }
    }
}
