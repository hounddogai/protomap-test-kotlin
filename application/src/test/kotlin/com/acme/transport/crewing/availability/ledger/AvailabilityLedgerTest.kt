package com.acme.transport.crewing.availability.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newAvailabilityLedger("code-196", "label-322")
        assertEquals(AvailabilityLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAvailabilityLedger("code-196", "label-322")
        try {
            item.moveTo(AvailabilityLedgerStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityLedgerStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAvailabilityLedger("code-196", "label-322")
        item.status = AvailabilityLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(AvailabilityLedgerStatus.IDLE) }
    }
}
