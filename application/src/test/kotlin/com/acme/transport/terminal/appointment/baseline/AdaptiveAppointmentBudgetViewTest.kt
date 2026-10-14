package com.acme.transport.terminal.appointment.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAppointmentBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAppointmentBudget("code-882", "title-409")
        val view = toAdaptiveAppointmentBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAppointmentBudgetViews(mutableListOf<AdaptiveAppointmentBudget>(item, item)).size, "views")
    }
}
