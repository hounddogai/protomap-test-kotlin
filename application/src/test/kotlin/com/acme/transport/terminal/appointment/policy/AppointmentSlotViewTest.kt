package com.acme.transport.terminal.appointment.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentSlot("code-784", "title-431")
        val view = toAppointmentSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentSlotViews(mutableListOf<AppointmentSlot>(item, item)).size, "views")
    }
}
