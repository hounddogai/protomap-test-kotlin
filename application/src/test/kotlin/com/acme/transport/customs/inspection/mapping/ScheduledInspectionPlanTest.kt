package com.acme.transport.customs.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledInspectionPlanTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledInspectionPlan("code-726", "name-412")
        assertEquals(ScheduledInspectionPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledInspectionPlan("code-726", "name-412")
        try {
            item.moveTo(ScheduledInspectionPlanStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledInspectionPlanStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledInspectionPlan("code-726", "name-412")
        item.status = ScheduledInspectionPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledInspectionPlanStatus.OPEN) }
    }
}
