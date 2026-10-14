package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictZoneScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictZoneSchedule("code-831", "label-123")
        assertEquals(StrictZoneScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newStrictZoneSchedule("code-831", "label-123")
        try {
            item.moveTo(StrictZoneScheduleStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictZoneScheduleStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictZoneSchedule("code-831", "label-123")
        item.status = StrictZoneScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(StrictZoneScheduleStatus.DRAFT) }
    }
}
