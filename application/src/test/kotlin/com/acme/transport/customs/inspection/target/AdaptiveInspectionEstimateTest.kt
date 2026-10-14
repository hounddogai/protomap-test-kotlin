package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveInspectionEstimate("code-307", "name-29")
        assertEquals(AdaptiveInspectionEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAdaptiveInspectionEstimate("code-307", "name-29")
        try {
            item.moveTo(AdaptiveInspectionEstimateStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionEstimateStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveInspectionEstimate("code-307", "name-29")
        item.status = AdaptiveInspectionEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveInspectionEstimateStatus.PLANNED) }
    }
}
