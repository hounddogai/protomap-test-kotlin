package com.acme.transport.terminal.crossdock.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newCrossDockAssignment("code-95", "name-462")
        assertEquals(CrossDockAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCrossDockAssignment("code-95", "name-462")
        try {
            item.moveTo(CrossDockAssignmentStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockAssignmentStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCrossDockAssignment("code-95", "name-462")
        item.status = CrossDockAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(CrossDockAssignmentStatus.REQUESTED) }
    }
}
