package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnClaim("code-693", "title-59")
        assertEquals(LeaseReturnClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLeaseReturnClaim("code-693", "title-59")
        try {
            item.moveTo(LeaseReturnClaimStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnClaimStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnClaim("code-693", "title-59")
        item.status = LeaseReturnClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnClaimStatus.PLANNED) }
    }
}
