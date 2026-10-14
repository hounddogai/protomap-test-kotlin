package com.acme.transport.terminal.dwell.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDwellMetricTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveDwellMetric("code-503", "name-336")
        assertEquals(AdaptiveDwellMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newAdaptiveDwellMetric("code-503", "name-336")
        try {
            item.moveTo(AdaptiveDwellMetricStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDwellMetricStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveDwellMetric("code-503", "name-336")
        item.status = AdaptiveDwellMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveDwellMetricStatus.OPEN) }
    }
}
