package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newDockDoorLedger("code-6", "label-807")
        assertEquals(DockDoorLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDockDoorLedger("code-6", "label-807")
        try {
            item.moveTo(DockDoorLedgerStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorLedgerStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDockDoorLedger("code-6", "label-807")
        item.status = DockDoorLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(DockDoorLedgerStatus.PLANNED) }
    }
}
