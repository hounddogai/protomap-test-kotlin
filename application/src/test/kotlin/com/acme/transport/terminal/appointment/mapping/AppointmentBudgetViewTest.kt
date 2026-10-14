package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentBudget("code-358", "title-880")
        val view = toAppointmentBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentBudgetViews(mutableListOf<AppointmentBudget>(item, item)).size, "views")
    }
}
