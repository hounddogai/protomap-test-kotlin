package com.acme.transport.terminal.appointment.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentTarget("code-228", "name-10")
        val view = toAppointmentTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentTargetViews(mutableListOf<AppointmentTarget>(item, item)).size, "views")
    }
}
