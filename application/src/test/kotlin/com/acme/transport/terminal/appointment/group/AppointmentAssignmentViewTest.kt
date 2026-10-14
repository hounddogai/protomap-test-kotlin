package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentAssignment("code-81", "name-671")
        val view = toAppointmentAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentAssignmentViews(mutableListOf<AppointmentAssignment>(item, item)).size, "views")
    }
}
