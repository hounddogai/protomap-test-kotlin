package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionEstimate("code-800", "label-137")
        assertEquals(InspectionEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newInspectionEstimate("code-800", "label-137")
        try {
            item.moveTo(InspectionEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionEstimate("code-800", "label-137")
        item.status = InspectionEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionEstimateStatus.PROPOSED) }
    }
}
