package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLeaseReturnRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedLeaseReturnRollup("code-713", "label-283")
        assertEquals(CappedLeaseReturnRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCappedLeaseReturnRollup("code-713", "label-283")
        try {
            item.moveTo(CappedLeaseReturnRollupStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLeaseReturnRollupStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedLeaseReturnRollup("code-713", "label-283")
        item.status = CappedLeaseReturnRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedLeaseReturnRollupStatus.PROPOSED) }
    }
}
