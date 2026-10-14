package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalInspectionScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalInspectionSchedule("code-130", "name-81")
        assertEquals(RegionalInspectionScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalInspectionSchedule("code-130", "name-81")
        try {
            item.moveTo(RegionalInspectionScheduleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalInspectionScheduleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalInspectionSchedule("code-130", "name-81")
        item.status = RegionalInspectionScheduleStatus.FAILED
        assertFails("move") { item.moveTo(RegionalInspectionScheduleStatus.NEW) }
    }
}
