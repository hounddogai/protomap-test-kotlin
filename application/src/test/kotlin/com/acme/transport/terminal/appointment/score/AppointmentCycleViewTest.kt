package com.acme.transport.terminal.appointment.score

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentCycle("code-526", "name-400")
        val view = toAppointmentCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentCycleViews(mutableListOf<AppointmentCycle>(item, item)).size, "views")
    }
}
