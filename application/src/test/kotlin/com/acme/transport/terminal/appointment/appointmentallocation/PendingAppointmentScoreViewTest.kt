package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAppointmentScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAppointmentScore("code-344", "name-153")
        val view = toPendingAppointmentScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAppointmentScoreViews(mutableListOf<PendingAppointmentScore>(item, item)).size, "views")
    }
}
