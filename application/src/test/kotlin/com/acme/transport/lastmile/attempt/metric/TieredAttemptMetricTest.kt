package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAttemptMetricTest {
    @Test
    fun `starts in new`() {
        val item = newTieredAttemptMetric("code-390", "label-947")
        assertEquals(TieredAttemptMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredAttemptMetric("code-390", "label-947")
        try {
            item.moveTo(TieredAttemptMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAttemptMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredAttemptMetric("code-390", "label-947")
        item.status = TieredAttemptMetricStatus.FAILED
        assertFails("move") { item.moveTo(TieredAttemptMetricStatus.NEW) }
    }
}
