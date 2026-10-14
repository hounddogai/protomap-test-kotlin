package com.acme.transport.terminal.appointment.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAppointmentBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAppointmentBaseline("code-11", "name-402")
        val view = toPendingAppointmentBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAppointmentBaselineViews(mutableListOf<PendingAppointmentBaseline>(item, item)).size, "views")
    }
}
