package com.acme.transport.terminal.appointment.appointmenttemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAppointmentTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAppointmentTemplate("code-124", "label-129")
        val view = toCappedAppointmentTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAppointmentTemplateViews(mutableListOf<CappedAppointmentTemplate>(item, item)).size, "views")
    }
}
