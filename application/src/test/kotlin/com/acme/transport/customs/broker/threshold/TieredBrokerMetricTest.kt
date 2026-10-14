package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredBrokerMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredBrokerMetric("code-806", "title-191")
        assertEquals(TieredBrokerMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTieredBrokerMetric("code-806", "title-191")
        try {
            item.moveTo(TieredBrokerMetricStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredBrokerMetricStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredBrokerMetric("code-806", "title-191")
        item.status = TieredBrokerMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredBrokerMetricStatus.PROPOSED) }
    }
}
