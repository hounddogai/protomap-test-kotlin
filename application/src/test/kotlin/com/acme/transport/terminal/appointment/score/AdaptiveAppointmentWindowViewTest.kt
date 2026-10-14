package com.acme.transport.terminal.appointment.score

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAppointmentWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAppointmentWindow("code-148", "title-385")
        val view = toAdaptiveAppointmentWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAppointmentWindowViews(mutableListOf<AdaptiveAppointmentWindow>(item, item)).size, "views")
    }
}
