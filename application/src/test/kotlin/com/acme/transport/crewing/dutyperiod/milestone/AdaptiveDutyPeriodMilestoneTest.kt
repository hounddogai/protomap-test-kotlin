package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDutyPeriodMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveDutyPeriodMilestone("code-254", "title-233")
        assertEquals(AdaptiveDutyPeriodMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveDutyPeriodMilestone("code-254", "title-233")
        try {
            item.moveTo(AdaptiveDutyPeriodMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDutyPeriodMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveDutyPeriodMilestone("code-254", "title-233")
        item.status = AdaptiveDutyPeriodMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveDutyPeriodMilestoneStatus.REQUESTED) }
    }
}
