package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newIncidentSchedule("code-956", "title-664")
        assertEquals(IncidentScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newIncidentSchedule("code-956", "title-664")
        try {
            item.moveTo(IncidentScheduleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentScheduleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIncidentSchedule("code-956", "title-664")
        item.status = IncidentScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(IncidentScheduleStatus.PLANNED) }
    }
}
