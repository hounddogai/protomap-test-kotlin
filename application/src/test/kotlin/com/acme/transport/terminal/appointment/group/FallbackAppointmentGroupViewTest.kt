package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAppointmentGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAppointmentGroup("code-543", "label-878")
        val view = toFallbackAppointmentGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAppointmentGroupViews(mutableListOf<FallbackAppointmentGroup>(item, item)).size, "views")
    }
}
