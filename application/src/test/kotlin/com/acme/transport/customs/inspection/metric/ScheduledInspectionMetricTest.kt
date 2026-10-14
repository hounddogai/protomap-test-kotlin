package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledInspectionMetricTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledInspectionMetric("code-711", "name-417")
        assertEquals(ScheduledInspectionMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledInspectionMetric("code-711", "name-417")
        try {
            item.moveTo(ScheduledInspectionMetricStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledInspectionMetricStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledInspectionMetric("code-711", "name-417")
        item.status = ScheduledInspectionMetricStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledInspectionMetricStatus.NEW) }
    }
}
