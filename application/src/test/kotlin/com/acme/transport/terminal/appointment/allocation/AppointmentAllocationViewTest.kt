package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentAllocation("code-208", "title-740")
        val view = toAppointmentAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentAllocationViews(mutableListOf<AppointmentAllocation>(item, item)).size, "views")
    }
}
