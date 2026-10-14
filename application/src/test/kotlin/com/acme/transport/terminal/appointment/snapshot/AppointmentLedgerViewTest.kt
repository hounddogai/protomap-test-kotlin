package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class AppointmentLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAppointmentLedger("code-773", "name-957")
        val view = toAppointmentLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAppointmentLedgerViews(mutableListOf<AppointmentLedger>(item, item)).size, "views")
    }
}
