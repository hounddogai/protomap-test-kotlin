package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentSummary("code-120", "name-711")
        val view = toAppointmentSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentSummaryViews(mutableListOf<AppointmentSummary>(item, item)).size, "views")
    }
}
