package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionEstimate("code-405", "title-178")
        assertEquals(InspectionEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newInspectionEstimate("code-405", "title-178")
        try {
            item.moveTo(InspectionEstimateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionEstimateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionEstimate("code-405", "title-178")
        item.status = InspectionEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionEstimateStatus.PLANNED) }
    }
}
