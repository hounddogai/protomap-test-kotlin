package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newDutyPeriodPlan("code-485", "label-745")
        assertEquals(DutyPeriodPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDutyPeriodPlan("code-485", "label-745")
        try {
            item.moveTo(DutyPeriodPlanStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodPlanStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDutyPeriodPlan("code-485", "label-745")
        item.status = DutyPeriodPlanStatus.STOPPED
        assertFails("move") { item.moveTo(DutyPeriodPlanStatus.IDLE) }
    }
}
