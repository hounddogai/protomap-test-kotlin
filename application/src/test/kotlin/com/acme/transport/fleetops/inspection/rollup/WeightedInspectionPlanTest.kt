package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedInspectionPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedInspectionPlan("code-900", "name-158")
        assertEquals(WeightedInspectionPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newWeightedInspectionPlan("code-900", "name-158")
        try {
            item.moveTo(WeightedInspectionPlanStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedInspectionPlanStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedInspectionPlan("code-900", "name-158")
        item.status = WeightedInspectionPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedInspectionPlanStatus.PLANNED) }
    }
}
