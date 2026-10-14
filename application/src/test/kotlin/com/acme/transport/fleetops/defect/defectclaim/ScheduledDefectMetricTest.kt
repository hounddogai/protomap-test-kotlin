package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDefectMetricTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledDefectMetric("code-224", "title-547")
        assertEquals(ScheduledDefectMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newScheduledDefectMetric("code-224", "title-547")
        try {
            item.moveTo(ScheduledDefectMetricStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDefectMetricStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledDefectMetric("code-224", "title-547")
        item.status = ScheduledDefectMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledDefectMetricStatus.OPEN) }
    }
}
