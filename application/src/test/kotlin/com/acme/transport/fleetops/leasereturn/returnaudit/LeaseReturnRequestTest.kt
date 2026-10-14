package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newLeaseReturnRequest("code-636", "title-902")
        assertEquals(LeaseReturnRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLeaseReturnRequest("code-636", "title-902")
        try {
            item.moveTo(LeaseReturnRequestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnRequestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLeaseReturnRequest("code-636", "title-902")
        item.status = LeaseReturnRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(LeaseReturnRequestStatus.REQUESTED) }
    }
}
