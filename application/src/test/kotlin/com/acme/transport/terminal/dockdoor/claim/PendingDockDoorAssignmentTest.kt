package com.acme.transport.terminal.dockdoor.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDockDoorAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingDockDoorAssignment("code-51", "name-567")
        assertEquals(PendingDockDoorAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingDockDoorAssignment("code-51", "name-567")
        try {
            item.moveTo(PendingDockDoorAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDockDoorAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingDockDoorAssignment("code-51", "name-567")
        item.status = PendingDockDoorAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingDockDoorAssignmentStatus.REQUESTED) }
    }
}
