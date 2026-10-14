package com.acme.transport.network.node.nodesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyNodeMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyNodeMetric("code-657", "label-311")
        assertEquals(LegacyNodeMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLegacyNodeMetric("code-657", "label-311")
        try {
            item.moveTo(LegacyNodeMetricStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyNodeMetricStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyNodeMetric("code-657", "label-311")
        item.status = LegacyNodeMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyNodeMetricStatus.PLANNED) }
    }
}
