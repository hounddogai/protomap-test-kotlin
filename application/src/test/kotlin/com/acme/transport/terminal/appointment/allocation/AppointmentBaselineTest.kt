package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newAppointmentBaseline("code-171", "label-908")
        assertEquals(AppointmentBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAppointmentBaseline("code-171", "label-908")
        try {
            item.moveTo(AppointmentBaselineStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentBaselineStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAppointmentBaseline("code-171", "label-908")
        item.status = AppointmentBaselineStatus.FAILED
        assertFails("move") { item.moveTo(AppointmentBaselineStatus.NEW) }
    }
}
