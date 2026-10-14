package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAppointmentBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAppointmentBudget("code-440", "label-553")
        val view = toGlobalAppointmentBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAppointmentBudgetViews(mutableListOf<GlobalAppointmentBudget>(item, item)).size, "views")
    }
}
