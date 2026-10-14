package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLeaseReturnAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalLeaseReturnAdjustment("code-474", "name-970")
        assertEquals(GlobalLeaseReturnAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newGlobalLeaseReturnAdjustment("code-474", "name-970")
        try {
            item.moveTo(GlobalLeaseReturnAdjustmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLeaseReturnAdjustmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalLeaseReturnAdjustment("code-474", "name-970")
        item.status = GlobalLeaseReturnAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalLeaseReturnAdjustmentStatus.PROPOSED) }
    }
}
