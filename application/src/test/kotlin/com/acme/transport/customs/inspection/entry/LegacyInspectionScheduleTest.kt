package com.acme.transport.customs.inspection.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyInspectionSchedule("code-587", "label-291")
        assertEquals(LegacyInspectionScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLegacyInspectionSchedule("code-587", "label-291")
        try {
            item.moveTo(LegacyInspectionScheduleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionScheduleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyInspectionSchedule("code-587", "label-291")
        item.status = LegacyInspectionScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyInspectionScheduleStatus.OPEN) }
    }
}
