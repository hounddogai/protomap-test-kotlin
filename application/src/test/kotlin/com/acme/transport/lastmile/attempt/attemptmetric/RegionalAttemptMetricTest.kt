package com.acme.transport.lastmile.attempt.attemptmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalAttemptMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalAttemptMetric("code-3", "title-728")
        assertEquals(RegionalAttemptMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalAttemptMetric("code-3", "title-728")
        try {
            item.moveTo(RegionalAttemptMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalAttemptMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalAttemptMetric("code-3", "title-728")
        item.status = RegionalAttemptMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalAttemptMetricStatus.REQUESTED) }
    }
}
