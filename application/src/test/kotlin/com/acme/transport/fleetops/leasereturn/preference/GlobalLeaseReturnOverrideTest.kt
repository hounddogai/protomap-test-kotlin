package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLeaseReturnOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalLeaseReturnOverride("code-48", "label-713")
        assertEquals(GlobalLeaseReturnOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newGlobalLeaseReturnOverride("code-48", "label-713")
        try {
            item.moveTo(GlobalLeaseReturnOverrideStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLeaseReturnOverrideStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalLeaseReturnOverride("code-48", "label-713")
        item.status = GlobalLeaseReturnOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalLeaseReturnOverrideStatus.REQUESTED) }
    }
}
