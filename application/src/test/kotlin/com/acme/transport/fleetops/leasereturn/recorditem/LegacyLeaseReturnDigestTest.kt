package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLeaseReturnDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyLeaseReturnDigest("code-12", "name-863")
        assertEquals(LegacyLeaseReturnDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacyLeaseReturnDigest("code-12", "name-863")
        try {
            item.moveTo(LegacyLeaseReturnDigestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLeaseReturnDigestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyLeaseReturnDigest("code-12", "name-863")
        item.status = LegacyLeaseReturnDigestStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyLeaseReturnDigestStatus.IDLE) }
    }
}
