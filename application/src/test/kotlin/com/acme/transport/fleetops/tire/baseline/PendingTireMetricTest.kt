package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTireMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingTireMetric("code-702", "name-477")
        assertEquals(PendingTireMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingTireMetric("code-702", "name-477")
        try {
            item.moveTo(PendingTireMetricStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTireMetricStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingTireMetric("code-702", "name-477")
        item.status = PendingTireMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingTireMetricStatus.PLANNED) }
    }
}
