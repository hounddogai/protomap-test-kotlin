package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentQuota("code-591", "title-909")
        val view = toAppointmentQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentQuotaViews(mutableListOf<AppointmentQuota>(item, item)).size, "views")
    }
}
