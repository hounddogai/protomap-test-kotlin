package com.acme.transport.terminal.appointment.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newAppointmentSlot("code-784", "title-431")
        assertEquals(AppointmentSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAppointmentSlot("code-784", "title-431")
        try {
            item.moveTo(AppointmentSlotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentSlotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAppointmentSlot("code-784", "title-431")
        item.status = AppointmentSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(AppointmentSlotStatus.PLANNED) }
    }
}
