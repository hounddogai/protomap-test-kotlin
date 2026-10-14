package com.acme.transport.terminal.appointment.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentPreference("code-895", "label-392")
        val view = toAppointmentPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentPreferenceViews(mutableListOf<AppointmentPreference>(item, item)).size, "views")
    }
}
