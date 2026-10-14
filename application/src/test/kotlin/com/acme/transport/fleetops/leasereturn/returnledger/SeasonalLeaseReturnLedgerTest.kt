package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalLeaseReturnLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalLeaseReturnLedger("code-596", "title-571")
        assertEquals(SeasonalLeaseReturnLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalLeaseReturnLedger("code-596", "title-571")
        try {
            item.moveTo(SeasonalLeaseReturnLedgerStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalLeaseReturnLedgerStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalLeaseReturnLedger("code-596", "title-571")
        item.status = SeasonalLeaseReturnLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalLeaseReturnLedgerStatus.PROPOSED) }
    }
}
