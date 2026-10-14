package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDutyPeriodSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientDutyPeriodSegment("code-683", "label-362")
        assertEquals(LenientDutyPeriodSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientDutyPeriodSegment("code-683", "label-362")
        try {
            item.moveTo(LenientDutyPeriodSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDutyPeriodSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientDutyPeriodSegment("code-683", "label-362")
        item.status = LenientDutyPeriodSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientDutyPeriodSegmentStatus.PLANNED) }
    }
}
