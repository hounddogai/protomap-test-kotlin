package com.acme.transport.crewing.availability.availabilityledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAvailabilityLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newTieredAvailabilityLedger("code-539", "label-628")
        assertEquals(TieredAvailabilityLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTieredAvailabilityLedger("code-539", "label-628")
        try {
            item.moveTo(TieredAvailabilityLedgerStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAvailabilityLedgerStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredAvailabilityLedger("code-539", "label-628")
        item.status = TieredAvailabilityLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredAvailabilityLedgerStatus.OPEN) }
    }
}
