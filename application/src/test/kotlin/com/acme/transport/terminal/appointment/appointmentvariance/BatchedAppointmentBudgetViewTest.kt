package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAppointmentBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAppointmentBudget("code-507", "title-843")
        val view = toBatchedAppointmentBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAppointmentBudgetViews(mutableListOf<BatchedAppointmentBudget>(item, item)).size, "views")
    }
}
