package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultInspectionEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultInspectionEstimate("code-969", "name-678")
        assertEquals(DefaultInspectionEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDefaultInspectionEstimate("code-969", "name-678")
        try {
            item.moveTo(DefaultInspectionEstimateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultInspectionEstimateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultInspectionEstimate("code-969", "name-678")
        item.status = DefaultInspectionEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultInspectionEstimateStatus.PLANNED) }
    }
}
