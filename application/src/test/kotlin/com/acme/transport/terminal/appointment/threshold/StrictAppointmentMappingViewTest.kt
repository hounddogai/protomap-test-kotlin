package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAppointmentMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAppointmentMapping("code-43", "title-963")
        val view = toStrictAppointmentMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAppointmentMappingViews(mutableListOf<StrictAppointmentMapping>(item, item)).size, "views")
    }
}
