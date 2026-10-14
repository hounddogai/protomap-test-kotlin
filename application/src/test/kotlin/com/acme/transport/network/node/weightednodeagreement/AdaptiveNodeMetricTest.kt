package com.acme.transport.network.node.weightednodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNodeMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveNodeMetric("code-220", "title-384")
        assertEquals(AdaptiveNodeMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveNodeMetric("code-220", "title-384")
        try {
            item.moveTo(AdaptiveNodeMetricStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNodeMetricStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveNodeMetric("code-220", "title-384")
        item.status = AdaptiveNodeMetricStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveNodeMetricStatus.DRAFT) }
    }
}
