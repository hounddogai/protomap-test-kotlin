package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDockDoorAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackDockDoorAssignment("code-200", "name-382")
        assertEquals(FallbackDockDoorAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackDockDoorAssignment("code-200", "name-382")
        try {
            item.moveTo(FallbackDockDoorAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDockDoorAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackDockDoorAssignment("code-200", "name-382")
        item.status = FallbackDockDoorAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackDockDoorAssignmentStatus.REQUESTED) }
    }
}
