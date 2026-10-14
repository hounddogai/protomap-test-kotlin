package com.acme.transport.safety.incident.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredIncidentScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newTieredIncidentSchedule("code-985", "label-944")
        assertEquals(TieredIncidentScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newTieredIncidentSchedule("code-985", "label-944")
        try {
            item.moveTo(TieredIncidentScheduleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredIncidentScheduleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredIncidentSchedule("code-985", "label-944")
        item.status = TieredIncidentScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredIncidentScheduleStatus.OPEN) }
    }
}
