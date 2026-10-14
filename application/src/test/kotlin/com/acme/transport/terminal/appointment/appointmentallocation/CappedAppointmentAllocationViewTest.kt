package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAppointmentAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAppointmentAllocation("code-415", "label-872")
        val view = toCappedAppointmentAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAppointmentAllocationViews(mutableListOf<CappedAppointmentAllocation>(item, item)).size, "views")
    }
}
