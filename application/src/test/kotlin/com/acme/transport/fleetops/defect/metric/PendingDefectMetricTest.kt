package com.acme.transport.fleetops.defect.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDefectMetricTest {
    @Test
    fun `starts in open`() {
        val item = newPendingDefectMetric("code-526", "name-733")
        assertEquals(PendingDefectMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPendingDefectMetric("code-526", "name-733")
        try {
            item.moveTo(PendingDefectMetricStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDefectMetricStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingDefectMetric("code-526", "name-733")
        item.status = PendingDefectMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingDefectMetricStatus.OPEN) }
    }
}
