package com.acme.transport.network.node.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackNodeMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackNodeMetric("code-722", "title-820")
        assertEquals(FallbackNodeMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackNodeMetric("code-722", "title-820")
        try {
            item.moveTo(FallbackNodeMetricStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackNodeMetricStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackNodeMetric("code-722", "title-820")
        item.status = FallbackNodeMetricStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackNodeMetricStatus.IDLE) }
    }
}
