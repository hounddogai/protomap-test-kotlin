package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultInspectionScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultInspectionSchedule("code-259", "name-34")
        assertEquals(DefaultInspectionScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultInspectionSchedule("code-259", "name-34")
        try {
            item.moveTo(DefaultInspectionScheduleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultInspectionScheduleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultInspectionSchedule("code-259", "name-34")
        item.status = DefaultInspectionScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultInspectionScheduleStatus.OPEN) }
    }
}
