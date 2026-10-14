package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDisputeGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedDisputeGroup("code-384", "title-797")
        assertEquals(CappedDisputeGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedDisputeGroup("code-384", "title-797")
        try {
            item.moveTo(CappedDisputeGroupStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDisputeGroupStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedDisputeGroup("code-384", "title-797")
        item.status = CappedDisputeGroupStatus.CLOSED
        assertFails("move") { item.moveTo(CappedDisputeGroupStatus.DRAFT) }
    }
}
