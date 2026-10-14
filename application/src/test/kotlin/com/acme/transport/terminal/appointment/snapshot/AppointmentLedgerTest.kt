package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newAppointmentLedger("code-773", "name-957")
        assertEquals(AppointmentLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAppointmentLedger("code-773", "name-957")
        try {
            item.moveTo(AppointmentLedgerStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentLedgerStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAppointmentLedger("code-773", "name-957")
        item.status = AppointmentLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(AppointmentLedgerStatus.IDLE) }
    }
}
