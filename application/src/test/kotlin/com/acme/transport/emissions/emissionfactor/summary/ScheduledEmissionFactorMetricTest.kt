package com.acme.transport.emissions.emissionfactor.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledEmissionFactorMetricTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledEmissionFactorMetric("code-531", "label-211")
        assertEquals(ScheduledEmissionFactorMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newScheduledEmissionFactorMetric("code-531", "label-211")
        try {
            item.moveTo(ScheduledEmissionFactorMetricStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledEmissionFactorMetricStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledEmissionFactorMetric("code-531", "label-211")
        item.status = ScheduledEmissionFactorMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledEmissionFactorMetricStatus.OPEN) }
    }
}
