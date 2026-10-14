package com.acme.transport.freightpay.dispute.disputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDisputePolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedDisputePolicy("code-834", "title-45")
        assertEquals(CappedDisputePolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newCappedDisputePolicy("code-834", "title-45")
        try {
            item.moveTo(CappedDisputePolicyStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDisputePolicyStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedDisputePolicy("code-834", "title-45")
        item.status = CappedDisputePolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedDisputePolicyStatus.PLANNED) }
    }
}
