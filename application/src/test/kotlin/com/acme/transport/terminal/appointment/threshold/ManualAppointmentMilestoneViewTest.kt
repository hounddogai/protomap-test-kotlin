package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAppointmentMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAppointmentMilestone("code-671", "title-549")
        val view = toManualAppointmentMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAppointmentMilestoneViews(mutableListOf<ManualAppointmentMilestone>(item, item)).size, "views")
    }
}
