package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDisputeMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientDisputeMetric("code-154", "title-3")
        assertEquals(LenientDisputeMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLenientDisputeMetric("code-154", "title-3")
        try {
            item.moveTo(LenientDisputeMetricStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDisputeMetricStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientDisputeMetric("code-154", "title-3")
        item.status = LenientDisputeMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientDisputeMetricStatus.PROPOSED) }
    }
}
