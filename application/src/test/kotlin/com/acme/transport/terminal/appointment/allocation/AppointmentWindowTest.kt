package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newAppointmentWindow("code-790", "title-30")
        assertEquals(AppointmentWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAppointmentWindow("code-790", "title-30")
        try {
            item.moveTo(AppointmentWindowStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentWindowStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAppointmentWindow("code-790", "title-30")
        item.status = AppointmentWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AppointmentWindowStatus.PROPOSED) }
    }
}
