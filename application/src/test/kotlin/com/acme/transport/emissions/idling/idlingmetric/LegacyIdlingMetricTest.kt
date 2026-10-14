package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIdlingMetricTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyIdlingMetric("code-536", "label-255")
        assertEquals(LegacyIdlingMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyIdlingMetric("code-536", "label-255")
        try {
            item.moveTo(LegacyIdlingMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIdlingMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyIdlingMetric("code-536", "label-255")
        item.status = LegacyIdlingMetricStatus.FAILED
        assertFails("move") { item.moveTo(LegacyIdlingMetricStatus.NEW) }
    }
}
