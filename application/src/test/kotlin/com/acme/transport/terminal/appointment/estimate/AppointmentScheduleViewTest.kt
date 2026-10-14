package com.acme.transport.terminal.appointment.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentSchedule("code-955", "name-727")
        val view = toAppointmentScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentScheduleViews(mutableListOf<AppointmentSchedule>(item, item)).size, "views")
    }
}
