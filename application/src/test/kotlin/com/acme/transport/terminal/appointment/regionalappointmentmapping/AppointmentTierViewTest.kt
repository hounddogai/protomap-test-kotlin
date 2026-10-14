package com.acme.transport.terminal.appointment.regionalappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentTier("code-958", "label-3")
        val view = toAppointmentTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentTierViews(mutableListOf<AppointmentTier>(item, item)).size, "views")
    }
}
