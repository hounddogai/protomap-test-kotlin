package com.acme.transport.fleetops.tire.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newTireSchedule("code-370", "label-510")
        assertEquals(TireScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newTireSchedule("code-370", "label-510")
        try {
            item.moveTo(TireScheduleStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireScheduleStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTireSchedule("code-370", "label-510")
        item.status = TireScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(TireScheduleStatus.IDLE) }
    }
}
