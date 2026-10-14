package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionMilestone("code-501", "label-116")
        assertEquals(InspectionMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newInspectionMilestone("code-501", "label-116")
        try {
            item.moveTo(InspectionMilestoneStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionMilestoneStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionMilestone("code-501", "label-116")
        item.status = InspectionMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionMilestoneStatus.PLANNED) }
    }
}
