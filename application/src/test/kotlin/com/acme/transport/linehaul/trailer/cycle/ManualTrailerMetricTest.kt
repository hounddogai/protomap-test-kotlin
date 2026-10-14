package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newManualTrailerMetric("code-627", "name-657")
        assertEquals(ManualTrailerMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newManualTrailerMetric("code-627", "name-657")
        try {
            item.moveTo(ManualTrailerMetricStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerMetricStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualTrailerMetric("code-627", "name-657")
        item.status = ManualTrailerMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualTrailerMetricStatus.PLANNED) }
    }
}
