package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newAppointmentCheckpoint("code-437", "name-261")
        assertEquals(AppointmentCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAppointmentCheckpoint("code-437", "name-261")
        try {
            item.moveTo(AppointmentCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAppointmentCheckpoint("code-437", "name-261")
        item.status = AppointmentCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(AppointmentCheckpointStatus.CREATED) }
    }
}
