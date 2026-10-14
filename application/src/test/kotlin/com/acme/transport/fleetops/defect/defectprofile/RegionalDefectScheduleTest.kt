package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDefectScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalDefectSchedule("code-746", "label-299")
        assertEquals(RegionalDefectScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRegionalDefectSchedule("code-746", "label-299")
        try {
            item.moveTo(RegionalDefectScheduleStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDefectScheduleStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalDefectSchedule("code-746", "label-299")
        item.status = RegionalDefectScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalDefectScheduleStatus.REQUESTED) }
    }
}
