package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newAppointmentAllocation("code-208", "title-740")
        assertEquals(AppointmentAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAppointmentAllocation("code-208", "title-740")
        try {
            item.moveTo(AppointmentAllocationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentAllocationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAppointmentAllocation("code-208", "title-740")
        item.status = AppointmentAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(AppointmentAllocationStatus.PLANNED) }
    }
}
