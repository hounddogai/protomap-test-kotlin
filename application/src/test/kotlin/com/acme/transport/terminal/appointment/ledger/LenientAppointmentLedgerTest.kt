package com.acme.transport.terminal.appointment.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAppointmentLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newLenientAppointmentLedger("code-444", "name-53")
        assertEquals(LenientAppointmentLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLenientAppointmentLedger("code-444", "name-53")
        try {
            item.moveTo(LenientAppointmentLedgerStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAppointmentLedgerStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientAppointmentLedger("code-444", "name-53")
        item.status = LenientAppointmentLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientAppointmentLedgerStatus.OPEN) }
    }
}
