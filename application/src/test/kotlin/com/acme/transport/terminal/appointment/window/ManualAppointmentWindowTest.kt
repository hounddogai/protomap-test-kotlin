package com.acme.transport.terminal.appointment.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAppointmentWindowTest {
    @Test
    fun `starts in planned`() {
        val item = newManualAppointmentWindow("code-374", "label-362")
        assertEquals(ManualAppointmentWindowStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newManualAppointmentWindow("code-374", "label-362")
        try {
            item.moveTo(ManualAppointmentWindowStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAppointmentWindowStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualAppointmentWindow("code-374", "label-362")
        item.status = ManualAppointmentWindowStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualAppointmentWindowStatus.PLANNED) }
    }
}
