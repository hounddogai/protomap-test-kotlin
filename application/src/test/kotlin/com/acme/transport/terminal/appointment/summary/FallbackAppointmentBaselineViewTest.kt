package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAppointmentBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAppointmentBaseline("code-290", "title-521")
        val view = toFallbackAppointmentBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAppointmentBaselineViews(mutableListOf<FallbackAppointmentBaseline>(item, item)).size, "views")
    }
}
