package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalSortationLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalSortationLedger("code-128", "name-919")
        assertEquals(RegionalSortationLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRegionalSortationLedger("code-128", "name-919")
        try {
            item.moveTo(RegionalSortationLedgerStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalSortationLedgerStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalSortationLedger("code-128", "name-919")
        item.status = RegionalSortationLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalSortationLedgerStatus.IDLE) }
    }
}
