package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetMetricTest {
    @Test
    fun `starts in new`() {
        val item = newRouteOffsetMetric("code-69", "name-522")
        assertEquals(RouteOffsetMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRouteOffsetMetric("code-69", "name-522")
        try {
            item.moveTo(RouteOffsetMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRouteOffsetMetric("code-69", "name-522")
        item.status = RouteOffsetMetricStatus.FAILED
        assertFails("move") { item.moveTo(RouteOffsetMetricStatus.NEW) }
    }
}
