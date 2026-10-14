package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDisputeGroupTest {
    @Test
    fun `starts in new`() {
        val item = newRollingDisputeGroup("code-929", "title-430")
        assertEquals(RollingDisputeGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRollingDisputeGroup("code-929", "title-430")
        try {
            item.moveTo(RollingDisputeGroupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDisputeGroupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingDisputeGroup("code-929", "title-430")
        item.status = RollingDisputeGroupStatus.FAILED
        assertFails("move") { item.moveTo(RollingDisputeGroupStatus.NEW) }
    }
}
