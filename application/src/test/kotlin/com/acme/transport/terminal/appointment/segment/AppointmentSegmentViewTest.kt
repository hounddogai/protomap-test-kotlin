package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentSegment("code-568", "title-431")
        val view = toAppointmentSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentSegmentViews(mutableListOf<AppointmentSegment>(item, item)).size, "views")
    }
}
