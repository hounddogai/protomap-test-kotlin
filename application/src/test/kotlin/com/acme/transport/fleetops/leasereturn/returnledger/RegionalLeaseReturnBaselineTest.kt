package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLeaseReturnBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalLeaseReturnBaseline("code-423", "name-902")
        assertEquals(RegionalLeaseReturnBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRegionalLeaseReturnBaseline("code-423", "name-902")
        try {
            item.moveTo(RegionalLeaseReturnBaselineStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLeaseReturnBaselineStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalLeaseReturnBaseline("code-423", "name-902")
        item.status = RegionalLeaseReturnBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalLeaseReturnBaselineStatus.DRAFT) }
    }
}
