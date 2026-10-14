package com.acme.transport.customs.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionSchedule("code-920", "label-153")
        assertEquals(InspectionScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newInspectionSchedule("code-920", "label-153")
        try {
            item.moveTo(InspectionScheduleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionScheduleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionSchedule("code-920", "label-153")
        item.status = InspectionScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionScheduleStatus.DRAFT) }
    }
}
