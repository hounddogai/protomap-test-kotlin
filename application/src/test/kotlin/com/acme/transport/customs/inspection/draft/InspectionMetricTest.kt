package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionMetricTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionMetric("code-581", "label-348")
        assertEquals(InspectionMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newInspectionMetric("code-581", "label-348")
        try {
            item.moveTo(InspectionMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionMetric("code-581", "label-348")
        item.status = InspectionMetricStatus.FAILED
        assertFails("move") { item.moveTo(InspectionMetricStatus.NEW) }
    }
}
