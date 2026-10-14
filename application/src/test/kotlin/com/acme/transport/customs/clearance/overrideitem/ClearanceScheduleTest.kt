package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newClearanceSchedule("code-984", "name-242")
        assertEquals(ClearanceScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newClearanceSchedule("code-984", "name-242")
        try {
            item.moveTo(ClearanceScheduleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceScheduleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newClearanceSchedule("code-984", "name-242")
        item.status = ClearanceScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(ClearanceScheduleStatus.IDLE) }
    }
}
