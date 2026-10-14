package com.acme.transport.terminal.appointment.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalAppointmentOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalAppointmentOverride("code-278", "label-174")
        val view = toSeasonalAppointmentOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalAppointmentOverrideViews(mutableListOf<SeasonalAppointmentOverride>(item, item)).size, "views")
    }
}
