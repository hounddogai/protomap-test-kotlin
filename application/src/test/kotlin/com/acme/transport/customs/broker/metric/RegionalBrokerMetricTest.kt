package com.acme.transport.customs.broker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalBrokerMetricTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalBrokerMetric("code-516", "name-933")
        assertEquals(RegionalBrokerMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalBrokerMetric("code-516", "name-933")
        try {
            item.moveTo(RegionalBrokerMetricStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalBrokerMetricStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalBrokerMetric("code-516", "name-933")
        item.status = RegionalBrokerMetricStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalBrokerMetricStatus.CREATED) }
    }
}
