package com.acme.transport.fleetops.utilization.utilizationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryUtilizationLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryUtilizationLedger("code-15", "label-843")
        assertEquals(PrimaryUtilizationLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryUtilizationLedger("code-15", "label-843")
        try {
            item.moveTo(PrimaryUtilizationLedgerStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryUtilizationLedgerStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryUtilizationLedger("code-15", "label-843")
        item.status = PrimaryUtilizationLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryUtilizationLedgerStatus.IDLE) }
    }
}
