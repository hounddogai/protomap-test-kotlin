package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRouteOffsetMetricTest {
    @Test
    fun `starts in open`() {
        val item = newRollingRouteOffsetMetric("code-801", "title-519")
        assertEquals(RollingRouteOffsetMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newRollingRouteOffsetMetric("code-801", "title-519")
        try {
            item.moveTo(RollingRouteOffsetMetricStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRouteOffsetMetricStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingRouteOffsetMetric("code-801", "title-519")
        item.status = RollingRouteOffsetMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingRouteOffsetMetricStatus.OPEN) }
    }
}
