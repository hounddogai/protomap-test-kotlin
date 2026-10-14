package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalZoneScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalZoneSchedule("code-528", "title-843")
        assertEquals(GlobalZoneScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalZoneSchedule("code-528", "title-843")
        try {
            item.moveTo(GlobalZoneScheduleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalZoneScheduleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalZoneSchedule("code-528", "title-843")
        item.status = GlobalZoneScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalZoneScheduleStatus.CREATED) }
    }
}
