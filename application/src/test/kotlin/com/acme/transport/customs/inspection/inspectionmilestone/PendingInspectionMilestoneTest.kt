package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newPendingInspectionMilestone("code-79", "label-230")
        assertEquals(PendingInspectionMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingInspectionMilestone("code-79", "label-230")
        try {
            item.moveTo(PendingInspectionMilestoneStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionMilestoneStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingInspectionMilestone("code-79", "label-230")
        item.status = PendingInspectionMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(PendingInspectionMilestoneStatus.NEW) }
    }
}
