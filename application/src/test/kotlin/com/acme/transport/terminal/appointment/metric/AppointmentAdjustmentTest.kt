package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newAppointmentAdjustment("code-971", "label-105")
        assertEquals(AppointmentAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAppointmentAdjustment("code-971", "label-105")
        try {
            item.moveTo(AppointmentAdjustmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentAdjustmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAppointmentAdjustment("code-971", "label-105")
        item.status = AppointmentAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(AppointmentAdjustmentStatus.NEW) }
    }
}
