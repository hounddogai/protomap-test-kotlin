package com.acme.transport.terminal.appointment.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAppointmentLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAppointmentLedger("code-444", "name-53")
        val view = toLenientAppointmentLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAppointmentLedgerViews(mutableListOf<LenientAppointmentLedger>(item, item)).size, "views")
    }
}
