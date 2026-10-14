package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentWindow("code-790", "title-30")
        val view = toAppointmentWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentWindowViews(mutableListOf<AppointmentWindow>(item, item)).size, "views")
    }
}
