package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newClearanceAssignment("code-292", "title-671")
        assertEquals(ClearanceAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newClearanceAssignment("code-292", "title-671")
        try {
            item.moveTo(ClearanceAssignmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceAssignmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newClearanceAssignment("code-292", "title-671")
        item.status = ClearanceAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(ClearanceAssignmentStatus.PLANNED) }
    }
}
