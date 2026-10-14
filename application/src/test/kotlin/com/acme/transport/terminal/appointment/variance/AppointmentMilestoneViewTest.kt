package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentMilestone("code-83", "label-918")
        val view = toAppointmentMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentMilestoneViews(mutableListOf<AppointmentMilestone>(item, item)).size, "views")
    }
}
