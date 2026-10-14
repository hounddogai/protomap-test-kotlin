package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDisputePolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientDisputePolicy("code-949", "title-457")
        assertEquals(LenientDisputePolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientDisputePolicy("code-949", "title-457")
        try {
            item.moveTo(LenientDisputePolicyStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDisputePolicyStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientDisputePolicy("code-949", "title-457")
        item.status = LenientDisputePolicyStatus.CLOSED
        assertFails("move") { item.moveTo(LenientDisputePolicyStatus.DRAFT) }
    }
}
