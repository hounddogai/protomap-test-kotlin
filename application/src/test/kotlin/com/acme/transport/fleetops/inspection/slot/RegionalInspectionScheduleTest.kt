package com.acme.transport.fleetops.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalInspectionScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalInspectionSchedule("code-342", "label-99")
        assertEquals(RegionalInspectionScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRegionalInspectionSchedule("code-342", "label-99")
        try {
            item.moveTo(RegionalInspectionScheduleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalInspectionScheduleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalInspectionSchedule("code-342", "label-99")
        item.status = RegionalInspectionScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalInspectionScheduleStatus.OPEN) }
    }
}
