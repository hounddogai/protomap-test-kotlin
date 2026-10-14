package com.acme.transport.terminal.appointment.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentScore("code-306", "name-830")
        val view = toAppointmentScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentScoreViews(mutableListOf<AppointmentScore>(item, item)).size, "views")
    }
}
