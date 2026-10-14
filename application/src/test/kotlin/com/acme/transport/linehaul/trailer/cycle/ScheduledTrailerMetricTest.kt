package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTrailerMetricTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledTrailerMetric("code-713", "title-928")
        assertEquals(ScheduledTrailerMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledTrailerMetric("code-713", "title-928")
        try {
            item.moveTo(ScheduledTrailerMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTrailerMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledTrailerMetric("code-713", "title-928")
        item.status = ScheduledTrailerMetricStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledTrailerMetricStatus.NEW) }
    }
}
