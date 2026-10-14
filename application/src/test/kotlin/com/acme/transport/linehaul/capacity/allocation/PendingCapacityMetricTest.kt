package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCapacityMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingCapacityMetric("code-495", "title-933")
        assertEquals(PendingCapacityMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingCapacityMetric("code-495", "title-933")
        try {
            item.moveTo(PendingCapacityMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCapacityMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingCapacityMetric("code-495", "title-933")
        item.status = PendingCapacityMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingCapacityMetricStatus.REQUESTED) }
    }
}
