package com.acme.transport.fleetops.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newInspectionPlan("code-494", "name-335")
        assertEquals(InspectionPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newInspectionPlan("code-494", "name-335")
        try {
            item.moveTo(InspectionPlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionPlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newInspectionPlan("code-494", "name-335")
        item.status = InspectionPlanStatus.STOPPED
        assertFails("move") { item.moveTo(InspectionPlanStatus.IDLE) }
    }
}
