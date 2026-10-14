package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newDisputeProfile("code-679", "title-644")
        assertEquals(DisputeProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDisputeProfile("code-679", "title-644")
        try {
            item.moveTo(DisputeProfileStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeProfileStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDisputeProfile("code-679", "title-644")
        item.status = DisputeProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(DisputeProfileStatus.PLANNED) }
    }
}
