package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAppointmentCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAppointmentCycle("code-235", "label-454")
        val view = toAdaptiveAppointmentCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAppointmentCycleViews(mutableListOf<AdaptiveAppointmentCycle>(item, item)).size, "views")
    }
}
