package com.acme.transport.freightpay.dispute.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDisputeClaimTest {
    @Test
    fun `starts in new`() {
        val item = newCappedDisputeClaim("code-183", "label-409")
        assertEquals(CappedDisputeClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCappedDisputeClaim("code-183", "label-409")
        try {
            item.moveTo(CappedDisputeClaimStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDisputeClaimStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedDisputeClaim("code-183", "label-409")
        item.status = CappedDisputeClaimStatus.FAILED
        assertFails("move") { item.moveTo(CappedDisputeClaimStatus.NEW) }
    }
}
