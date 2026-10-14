package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAppointmentScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAppointmentScore("code-559", "title-107")
        val view = toBatchedAppointmentScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAppointmentScoreViews(mutableListOf<BatchedAppointmentScore>(item, item)).size, "views")
    }
}
