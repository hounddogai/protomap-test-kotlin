package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodSegment("code-956", "title-626")
        assertEquals(DutyPeriodSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDutyPeriodSegment("code-956", "title-626")
        try {
            item.moveTo(DutyPeriodSegmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodSegmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodSegment("code-956", "title-626")
        item.status = DutyPeriodSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodSegmentStatus.PROPOSED) }
    }
}
