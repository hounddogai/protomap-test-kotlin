package com.acme.transport.fleetops.defect.defectproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDefectAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackDefectAssignment("code-211", "name-472")
        assertEquals(FallbackDefectAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newFallbackDefectAssignment("code-211", "name-472")
        try {
            item.moveTo(FallbackDefectAssignmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDefectAssignmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackDefectAssignment("code-211", "name-472")
        item.status = FallbackDefectAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackDefectAssignmentStatus.PLANNED) }
    }
}
