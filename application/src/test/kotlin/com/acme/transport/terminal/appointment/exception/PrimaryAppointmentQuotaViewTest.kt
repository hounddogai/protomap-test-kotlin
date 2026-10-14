package com.acme.transport.terminal.appointment.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryAppointmentQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryAppointmentQuota("code-348", "name-507")
        val view = toPrimaryAppointmentQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryAppointmentQuotaViews(mutableListOf<PrimaryAppointmentQuota>(item, item)).size, "views")
    }
}
