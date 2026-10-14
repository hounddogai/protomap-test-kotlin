package com.acme.transport.lastmile.drop.dropentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDropMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackDropMetric("code-712", "label-189")
        assertEquals(FallbackDropMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFallbackDropMetric("code-712", "label-189")
        try {
            item.moveTo(FallbackDropMetricStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDropMetricStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackDropMetric("code-712", "label-189")
        item.status = FallbackDropMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackDropMetricStatus.PROPOSED) }
    }
}
