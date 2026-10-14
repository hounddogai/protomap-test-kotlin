package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newBrokerMetric("code-173", "label-592")
        assertEquals(BrokerMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBrokerMetric("code-173", "label-592")
        try {
            item.moveTo(BrokerMetricStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerMetricStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBrokerMetric("code-173", "label-592")
        item.status = BrokerMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BrokerMetricStatus.PROPOSED) }
    }
}
