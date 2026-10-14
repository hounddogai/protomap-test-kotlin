package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackBackhaulMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackBackhaulMetric("code-116", "label-741")
        assertEquals(FallbackBackhaulMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackBackhaulMetric("code-116", "label-741")
        try {
            item.moveTo(FallbackBackhaulMetricStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackBackhaulMetricStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackBackhaulMetric("code-116", "label-741")
        item.status = FallbackBackhaulMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackBackhaulMetricStatus.PROPOSED) }
    }
}
