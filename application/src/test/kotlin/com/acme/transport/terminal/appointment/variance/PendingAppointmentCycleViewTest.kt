package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAppointmentCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAppointmentCycle("code-906", "name-311")
        val view = toPendingAppointmentCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAppointmentCycleViews(mutableListOf<PendingAppointmentCycle>(item, item)).size, "views")
    }
}
