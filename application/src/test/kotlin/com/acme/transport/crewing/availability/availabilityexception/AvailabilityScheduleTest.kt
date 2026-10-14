package com.acme.transport.crewing.availability.availabilityexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newAvailabilitySchedule("code-139", "name-634")
        assertEquals(AvailabilityScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAvailabilitySchedule("code-139", "name-634")
        try {
            item.moveTo(AvailabilityScheduleStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityScheduleStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAvailabilitySchedule("code-139", "name-634")
        item.status = AvailabilityScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(AvailabilityScheduleStatus.IDLE) }
    }
}
