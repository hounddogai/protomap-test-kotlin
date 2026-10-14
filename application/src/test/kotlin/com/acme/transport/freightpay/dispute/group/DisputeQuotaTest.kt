package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newDisputeQuota("code-942", "title-606")
        assertEquals(DisputeQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newDisputeQuota("code-942", "title-606")
        try {
            item.moveTo(DisputeQuotaStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeQuotaStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDisputeQuota("code-942", "title-606")
        item.status = DisputeQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(DisputeQuotaStatus.DRAFT) }
    }
}
