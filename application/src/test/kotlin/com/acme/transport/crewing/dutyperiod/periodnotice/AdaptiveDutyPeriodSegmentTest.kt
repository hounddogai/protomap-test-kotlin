package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDutyPeriodSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveDutyPeriodSegment("code-24", "name-362")
        assertEquals(AdaptiveDutyPeriodSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveDutyPeriodSegment("code-24", "name-362")
        try {
            item.moveTo(AdaptiveDutyPeriodSegmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDutyPeriodSegmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveDutyPeriodSegment("code-24", "name-362")
        item.status = AdaptiveDutyPeriodSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveDutyPeriodSegmentStatus.IDLE) }
    }
}
