package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionSchedule("code-479", "title-771")
        assertEquals(InspectionScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newInspectionSchedule("code-479", "title-771")
        try {
            item.moveTo(InspectionScheduleStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionScheduleStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionSchedule("code-479", "title-771")
        item.status = InspectionScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionScheduleStatus.CREATED) }
    }
}
