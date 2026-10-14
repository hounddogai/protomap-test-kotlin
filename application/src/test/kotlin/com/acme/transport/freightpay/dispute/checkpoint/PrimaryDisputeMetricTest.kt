package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDisputeMetricTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryDisputeMetric("code-624", "label-58")
        assertEquals(PrimaryDisputeMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryDisputeMetric("code-624", "label-58")
        try {
            item.moveTo(PrimaryDisputeMetricStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDisputeMetricStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryDisputeMetric("code-624", "label-58")
        item.status = PrimaryDisputeMetricStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryDisputeMetricStatus.CREATED) }
    }
}
