package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newDefectAssignment("code-957", "name-818")
        assertEquals(DefectAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newDefectAssignment("code-957", "name-818")
        try {
            item.moveTo(DefectAssignmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectAssignmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefectAssignment("code-957", "name-818")
        item.status = DefectAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(DefectAssignmentStatus.NEW) }
    }
}
