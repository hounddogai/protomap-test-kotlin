package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAppointmentEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAppointmentEntry("code-160", "label-479")
        val view = toStrictAppointmentEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAppointmentEntryViews(mutableListOf<StrictAppointmentEntry>(item, item)).size, "views")
    }
}
