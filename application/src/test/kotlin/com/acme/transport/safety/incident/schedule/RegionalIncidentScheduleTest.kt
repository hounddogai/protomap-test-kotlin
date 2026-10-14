package com.acme.transport.safety.incident.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIncidentScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalIncidentSchedule("code-929", "name-840")
        assertEquals(RegionalIncidentScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRegionalIncidentSchedule("code-929", "name-840")
        try {
            item.moveTo(RegionalIncidentScheduleStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIncidentScheduleStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalIncidentSchedule("code-929", "name-840")
        item.status = RegionalIncidentScheduleStatus.FAILED
        assertFails("move") { item.moveTo(RegionalIncidentScheduleStatus.NEW) }
    }
}
