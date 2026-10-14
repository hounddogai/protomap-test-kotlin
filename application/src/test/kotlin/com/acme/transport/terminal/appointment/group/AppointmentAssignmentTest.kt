package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newAppointmentAssignment("code-81", "name-671")
        assertEquals(AppointmentAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAppointmentAssignment("code-81", "name-671")
        try {
            item.moveTo(AppointmentAssignmentStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentAssignmentStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAppointmentAssignment("code-81", "name-671")
        item.status = AppointmentAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(AppointmentAssignmentStatus.PLANNED) }
    }
}
