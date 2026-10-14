package com.acme.transport.customs.broker.brokerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveBrokerMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveBrokerMetric("code-175", "name-43")
        assertEquals(AdaptiveBrokerMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveBrokerMetric("code-175", "name-43")
        try {
            item.moveTo(AdaptiveBrokerMetricStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveBrokerMetricStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveBrokerMetric("code-175", "name-43")
        item.status = AdaptiveBrokerMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveBrokerMetricStatus.REQUESTED) }
    }
}
