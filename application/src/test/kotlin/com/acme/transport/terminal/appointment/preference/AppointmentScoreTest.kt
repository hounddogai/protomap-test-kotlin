package com.acme.transport.terminal.appointment.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newAppointmentScore("code-306", "name-830")
        assertEquals(AppointmentScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newAppointmentScore("code-306", "name-830")
        try {
            item.moveTo(AppointmentScoreStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentScoreStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAppointmentScore("code-306", "name-830")
        item.status = AppointmentScoreStatus.STOPPED
        assertFails("move") { item.moveTo(AppointmentScoreStatus.IDLE) }
    }
}
