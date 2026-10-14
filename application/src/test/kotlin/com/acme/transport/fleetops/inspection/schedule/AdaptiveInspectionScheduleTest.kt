package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveInspectionSchedule("code-837", "title-993")
        assertEquals(AdaptiveInspectionScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAdaptiveInspectionSchedule("code-837", "title-993")
        try {
            item.moveTo(AdaptiveInspectionScheduleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionScheduleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveInspectionSchedule("code-837", "title-993")
        item.status = AdaptiveInspectionScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveInspectionScheduleStatus.DRAFT) }
    }
}
