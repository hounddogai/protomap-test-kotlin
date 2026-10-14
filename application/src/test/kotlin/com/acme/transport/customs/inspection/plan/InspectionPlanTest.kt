package com.acme.transport.customs.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionPlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionPlan("code-87", "label-874")
        assertEquals(InspectionPlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newInspectionPlan("code-87", "label-874")
        try {
            item.moveTo(InspectionPlanStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionPlanStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionPlan("code-87", "label-874")
        item.status = InspectionPlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionPlanStatus.PROPOSED) }
    }
}
