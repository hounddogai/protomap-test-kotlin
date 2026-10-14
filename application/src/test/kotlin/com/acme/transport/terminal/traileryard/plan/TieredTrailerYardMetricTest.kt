package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTrailerYardMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredTrailerYardMetric("code-307", "label-530")
        assertEquals(TieredTrailerYardMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTieredTrailerYardMetric("code-307", "label-530")
        try {
            item.moveTo(TieredTrailerYardMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTrailerYardMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredTrailerYardMetric("code-307", "label-530")
        item.status = TieredTrailerYardMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredTrailerYardMetricStatus.REQUESTED) }
    }
}
