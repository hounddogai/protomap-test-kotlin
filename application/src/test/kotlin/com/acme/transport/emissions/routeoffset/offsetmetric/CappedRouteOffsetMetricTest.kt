package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRouteOffsetMetricTest {
    @Test
    fun `starts in new`() {
        val item = newCappedRouteOffsetMetric("code-336", "name-553")
        assertEquals(CappedRouteOffsetMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCappedRouteOffsetMetric("code-336", "name-553")
        try {
            item.moveTo(CappedRouteOffsetMetricStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRouteOffsetMetricStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedRouteOffsetMetric("code-336", "name-553")
        item.status = CappedRouteOffsetMetricStatus.FAILED
        assertFails("move") { item.moveTo(CappedRouteOffsetMetricStatus.NEW) }
    }
}
