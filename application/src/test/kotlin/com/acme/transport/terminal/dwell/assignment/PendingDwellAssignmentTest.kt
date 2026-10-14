package com.acme.transport.terminal.dwell.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDwellAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingDwellAssignment("code-850", "title-533")
        assertEquals(PendingDwellAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingDwellAssignment("code-850", "title-533")
        try {
            item.moveTo(PendingDwellAssignmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDwellAssignmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingDwellAssignment("code-850", "title-533")
        item.status = PendingDwellAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingDwellAssignmentStatus.REQUESTED) }
    }
}
