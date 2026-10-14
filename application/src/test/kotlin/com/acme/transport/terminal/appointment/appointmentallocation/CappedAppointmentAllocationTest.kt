package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAppointmentAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newCappedAppointmentAllocation("code-415", "label-872")
        assertEquals(CappedAppointmentAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedAppointmentAllocation("code-415", "label-872")
        try {
            item.moveTo(CappedAppointmentAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAppointmentAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedAppointmentAllocation("code-415", "label-872")
        item.status = CappedAppointmentAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(CappedAppointmentAllocationStatus.CREATED) }
    }
}
