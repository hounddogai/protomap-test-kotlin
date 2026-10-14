package com.acme.transport.terminal.appointment.window

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAppointmentWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAppointmentWindow("code-374", "label-362")
        val view = toManualAppointmentWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAppointmentWindowViews(mutableListOf<ManualAppointmentWindow>(item, item)).size, "views")
    }
}
