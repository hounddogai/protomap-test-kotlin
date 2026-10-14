package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTireScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalTireSchedule("code-630", "name-328")
        assertEquals(GlobalTireScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newGlobalTireSchedule("code-630", "name-328")
        try {
            item.moveTo(GlobalTireScheduleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTireScheduleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalTireSchedule("code-630", "name-328")
        item.status = GlobalTireScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalTireScheduleStatus.IDLE) }
    }
}
