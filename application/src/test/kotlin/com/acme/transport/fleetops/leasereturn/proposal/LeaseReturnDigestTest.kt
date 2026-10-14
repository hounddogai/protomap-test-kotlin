package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnDigestTest {
    @Test
    fun `starts in open`() {
        val item = newLeaseReturnDigest("code-227", "name-688")
        assertEquals(LeaseReturnDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLeaseReturnDigest("code-227", "name-688")
        try {
            item.moveTo(LeaseReturnDigestStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnDigestStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLeaseReturnDigest("code-227", "name-688")
        item.status = LeaseReturnDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(LeaseReturnDigestStatus.OPEN) }
    }
}
