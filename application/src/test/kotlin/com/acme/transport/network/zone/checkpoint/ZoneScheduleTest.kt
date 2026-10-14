package com.acme.transport.network.zone.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newZoneSchedule("code-543", "name-806")
        assertEquals(ZoneScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newZoneSchedule("code-543", "name-806")
        try {
            item.moveTo(ZoneScheduleStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneScheduleStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newZoneSchedule("code-543", "name-806")
        item.status = ZoneScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ZoneScheduleStatus.PROPOSED) }
    }
}
