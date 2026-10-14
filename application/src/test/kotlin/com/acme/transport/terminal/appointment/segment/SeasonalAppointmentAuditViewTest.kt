package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalAppointmentAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalAppointmentAudit("code-485", "label-108")
        val view = toSeasonalAppointmentAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalAppointmentAuditViews(mutableListOf<SeasonalAppointmentAudit>(item, item)).size, "views")
    }
}
