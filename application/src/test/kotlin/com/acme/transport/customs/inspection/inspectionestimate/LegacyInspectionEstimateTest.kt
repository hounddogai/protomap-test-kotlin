package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyInspectionEstimate("code-460", "label-407")
        assertEquals(LegacyInspectionEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyInspectionEstimate("code-460", "label-407")
        try {
            item.moveTo(LegacyInspectionEstimateStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionEstimateStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyInspectionEstimate("code-460", "label-407")
        item.status = LegacyInspectionEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyInspectionEstimateStatus.IDLE) }
    }
}
