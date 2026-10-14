package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDisputeAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newRollingDisputeAdjustment("code-313", "name-891")
        assertEquals(RollingDisputeAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingDisputeAdjustment("code-313", "name-891")
        try {
            item.moveTo(RollingDisputeAdjustmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDisputeAdjustmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingDisputeAdjustment("code-313", "name-891")
        item.status = RollingDisputeAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(RollingDisputeAdjustmentStatus.CREATED) }
    }
}
