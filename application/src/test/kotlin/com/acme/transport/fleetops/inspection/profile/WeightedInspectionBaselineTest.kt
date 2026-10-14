package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedInspectionBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedInspectionBaseline("code-144", "label-855")
        assertEquals(WeightedInspectionBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newWeightedInspectionBaseline("code-144", "label-855")
        try {
            item.moveTo(WeightedInspectionBaselineStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedInspectionBaselineStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedInspectionBaseline("code-144", "label-855")
        item.status = WeightedInspectionBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedInspectionBaselineStatus.PLANNED) }
    }
}
