package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRosterAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newCappedRosterAssignment("code-657", "name-556")
        assertEquals(CappedRosterAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCappedRosterAssignment("code-657", "name-556")
        try {
            item.moveTo(CappedRosterAssignmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRosterAssignmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedRosterAssignment("code-657", "name-556")
        item.status = CappedRosterAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedRosterAssignmentStatus.OPEN) }
    }
}
