package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCrossDockAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledCrossDockAssignment("code-525", "title-603")
        assertEquals(ScheduledCrossDockAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScheduledCrossDockAssignment("code-525", "title-603")
        try {
            item.moveTo(ScheduledCrossDockAssignmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCrossDockAssignmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledCrossDockAssignment("code-525", "title-603")
        item.status = ScheduledCrossDockAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledCrossDockAssignmentStatus.REQUESTED) }
    }
}
