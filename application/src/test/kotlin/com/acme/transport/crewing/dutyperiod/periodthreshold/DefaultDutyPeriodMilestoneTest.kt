package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDutyPeriodMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultDutyPeriodMilestone("code-100", "name-706")
        assertEquals(DefaultDutyPeriodMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefaultDutyPeriodMilestone("code-100", "name-706")
        try {
            item.moveTo(DefaultDutyPeriodMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDutyPeriodMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultDutyPeriodMilestone("code-100", "name-706")
        item.status = DefaultDutyPeriodMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultDutyPeriodMilestoneStatus.IDLE) }
    }
}
