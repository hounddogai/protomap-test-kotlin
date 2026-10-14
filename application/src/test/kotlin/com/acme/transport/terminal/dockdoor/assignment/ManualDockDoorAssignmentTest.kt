package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDockDoorAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newManualDockDoorAssignment("code-594", "title-356")
        assertEquals(ManualDockDoorAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualDockDoorAssignment("code-594", "title-356")
        try {
            item.moveTo(ManualDockDoorAssignmentStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDockDoorAssignmentStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualDockDoorAssignment("code-594", "title-356")
        item.status = ManualDockDoorAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(ManualDockDoorAssignmentStatus.DRAFT) }
    }
}
