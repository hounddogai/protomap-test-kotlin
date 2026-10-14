package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyInspectionSchedule("code-868", "name-867")
        assertEquals(LegacyInspectionScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyInspectionSchedule("code-868", "name-867")
        try {
            item.moveTo(LegacyInspectionScheduleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionScheduleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyInspectionSchedule("code-868", "name-867")
        item.status = LegacyInspectionScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyInspectionScheduleStatus.REQUESTED) }
    }
}
