package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newDockDoorAssignment("code-312", "name-469")
        assertEquals(DockDoorAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDockDoorAssignment("code-312", "name-469")
        try {
            item.moveTo(DockDoorAssignmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorAssignmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDockDoorAssignment("code-312", "name-469")
        item.status = DockDoorAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DockDoorAssignmentStatus.PROPOSED) }
    }
}
