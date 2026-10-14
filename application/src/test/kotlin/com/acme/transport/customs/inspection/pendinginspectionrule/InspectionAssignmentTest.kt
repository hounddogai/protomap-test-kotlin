package com.acme.transport.customs.inspection.pendinginspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newInspectionAssignment("code-374", "label-545")
        assertEquals(InspectionAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newInspectionAssignment("code-374", "label-545")
        try {
            item.moveTo(InspectionAssignmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionAssignmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newInspectionAssignment("code-374", "label-545")
        item.status = InspectionAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(InspectionAssignmentStatus.IDLE) }
    }
}
