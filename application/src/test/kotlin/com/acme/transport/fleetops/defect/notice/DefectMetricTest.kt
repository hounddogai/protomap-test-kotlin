package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectMetricTest {
    @Test
    fun `starts in created`() {
        val item = newDefectMetric("code-408", "name-609")
        assertEquals(DefectMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDefectMetric("code-408", "name-609")
        try {
            item.moveTo(DefectMetricStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectMetricStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefectMetric("code-408", "name-609")
        item.status = DefectMetricStatus.RETIRED
        assertFails("move") { item.moveTo(DefectMetricStatus.CREATED) }
    }
}
