package com.acme.transport.fleetops.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualInspectionPlanTest {
    @Test
    fun `starts in created`() {
        val item = newManualInspectionPlan("code-502", "label-530")
        assertEquals(ManualInspectionPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newManualInspectionPlan("code-502", "label-530")
        try {
            item.moveTo(ManualInspectionPlanStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualInspectionPlanStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualInspectionPlan("code-502", "label-530")
        item.status = ManualInspectionPlanStatus.RETIRED
        assertFails("move") { item.moveTo(ManualInspectionPlanStatus.CREATED) }
    }
}
