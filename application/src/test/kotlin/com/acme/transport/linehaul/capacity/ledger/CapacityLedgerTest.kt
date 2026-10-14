package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newCapacityLedger("code-118", "label-435")
        assertEquals(CapacityLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCapacityLedger("code-118", "label-435")
        try {
            item.moveTo(CapacityLedgerStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityLedgerStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCapacityLedger("code-118", "label-435")
        item.status = CapacityLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(CapacityLedgerStatus.IDLE) }
    }
}
