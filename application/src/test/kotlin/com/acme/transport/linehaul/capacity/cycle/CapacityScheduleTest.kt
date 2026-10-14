package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newCapacitySchedule("code-335", "name-260")
        assertEquals(CapacityScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newCapacitySchedule("code-335", "name-260")
        try {
            item.moveTo(CapacityScheduleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityScheduleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCapacitySchedule("code-335", "name-260")
        item.status = CapacityScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(CapacityScheduleStatus.IDLE) }
    }
}
