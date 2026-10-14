package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentMapping("code-525", "title-647")
        val view = toAppointmentMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentMappingViews(mutableListOf<AppointmentMapping>(item, item)).size, "views")
    }
}
