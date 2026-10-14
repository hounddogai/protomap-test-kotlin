package com.acme.transport.freightpay.dispute.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDisputeMetricTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledDisputeMetric("code-146", "name-551")
        assertEquals(ScheduledDisputeMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledDisputeMetric("code-146", "name-551")
        try {
            item.moveTo(ScheduledDisputeMetricStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDisputeMetricStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledDisputeMetric("code-146", "name-551")
        item.status = ScheduledDisputeMetricStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledDisputeMetricStatus.CREATED) }
    }
}
