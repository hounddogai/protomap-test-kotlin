package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newDisputeClaim("code-251", "label-389")
        assertEquals(DisputeClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newDisputeClaim("code-251", "label-389")
        try {
            item.moveTo(DisputeClaimStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeClaimStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDisputeClaim("code-251", "label-389")
        item.status = DisputeClaimStatus.STOPPED
        assertFails("move") { item.moveTo(DisputeClaimStatus.IDLE) }
    }
}
