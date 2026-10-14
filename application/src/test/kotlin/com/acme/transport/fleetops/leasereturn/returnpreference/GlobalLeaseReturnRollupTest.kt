package com.acme.transport.fleetops.leasereturn.returnpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLeaseReturnRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalLeaseReturnRollup("code-526", "label-193")
        assertEquals(GlobalLeaseReturnRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newGlobalLeaseReturnRollup("code-526", "label-193")
        try {
            item.moveTo(GlobalLeaseReturnRollupStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLeaseReturnRollupStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalLeaseReturnRollup("code-526", "label-193")
        item.status = GlobalLeaseReturnRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalLeaseReturnRollupStatus.REQUESTED) }
    }
}
