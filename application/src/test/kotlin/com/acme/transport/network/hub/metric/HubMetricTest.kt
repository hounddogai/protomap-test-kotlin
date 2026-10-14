package com.acme.transport.network.hub.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newHubMetric("code-228", "name-769")
        assertEquals(HubMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newHubMetric("code-228", "name-769")
        try {
            item.moveTo(HubMetricStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubMetricStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHubMetric("code-228", "name-769")
        item.status = HubMetricStatus.STOPPED
        assertFails("move") { item.moveTo(HubMetricStatus.IDLE) }
    }
}
