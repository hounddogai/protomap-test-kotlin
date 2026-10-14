package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newDutyPeriodBudget("code-866", "label-19")
        assertEquals(DutyPeriodBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDutyPeriodBudget("code-866", "label-19")
        try {
            item.moveTo(DutyPeriodBudgetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodBudgetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDutyPeriodBudget("code-866", "label-19")
        item.status = DutyPeriodBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(DutyPeriodBudgetStatus.CREATED) }
    }
}
