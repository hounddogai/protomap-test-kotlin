package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newLeaseReturnRecord("code-273", "title-590")
        assertEquals(LeaseReturnRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLeaseReturnRecord("code-273", "title-590")
        try {
            item.moveTo(LeaseReturnRecordStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnRecordStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLeaseReturnRecord("code-273", "title-590")
        item.status = LeaseReturnRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(LeaseReturnRecordStatus.REQUESTED) }
    }
}
