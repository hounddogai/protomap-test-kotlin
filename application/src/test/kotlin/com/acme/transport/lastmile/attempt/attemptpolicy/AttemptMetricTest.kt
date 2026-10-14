package com.acme.transport.lastmile.attempt.attemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newAttemptMetric("code-753", "label-802")
        assertEquals(AttemptMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newAttemptMetric("code-753", "label-802")
        try {
            item.moveTo(AttemptMetricStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptMetricStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAttemptMetric("code-753", "label-802")
        item.status = AttemptMetricStatus.STOPPED
        assertFails("move") { item.moveTo(AttemptMetricStatus.IDLE) }
    }
}
