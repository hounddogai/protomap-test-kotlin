package com.acme.transport.terminal.appointment.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newAppointmentSchedule("code-955", "name-727")
        assertEquals(AppointmentScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newAppointmentSchedule("code-955", "name-727")
        try {
            item.moveTo(AppointmentScheduleStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentScheduleStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAppointmentSchedule("code-955", "name-727")
        item.status = AppointmentScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(AppointmentScheduleStatus.REQUESTED) }
    }
}
