package com.acme.transport.freightpay.dispute.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDisputeProfileTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyDisputeProfile("code-643", "label-244")
        assertEquals(LegacyDisputeProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLegacyDisputeProfile("code-643", "label-244")
        try {
            item.moveTo(LegacyDisputeProfileStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDisputeProfileStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyDisputeProfile("code-643", "label-244")
        item.status = LegacyDisputeProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyDisputeProfileStatus.OPEN) }
    }
}
