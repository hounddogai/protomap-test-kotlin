package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDisputeRequestTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyDisputeRequest("code-891", "label-548")
        assertEquals(LegacyDisputeRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLegacyDisputeRequest("code-891", "label-548")
        try {
            item.moveTo(LegacyDisputeRequestStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDisputeRequestStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyDisputeRequest("code-891", "label-548")
        item.status = LegacyDisputeRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyDisputeRequestStatus.OPEN) }
    }
}
