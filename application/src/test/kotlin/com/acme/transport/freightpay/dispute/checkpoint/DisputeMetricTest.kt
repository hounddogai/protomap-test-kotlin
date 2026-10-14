package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newDisputeMetric("code-399", "label-490")
        assertEquals(DisputeMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDisputeMetric("code-399", "label-490")
        try {
            item.moveTo(DisputeMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDisputeMetric("code-399", "label-490")
        item.status = DisputeMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(DisputeMetricStatus.REQUESTED) }
    }
}
