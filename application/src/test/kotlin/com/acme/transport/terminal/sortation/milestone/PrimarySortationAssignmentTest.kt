package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimarySortationAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimarySortationAssignment("code-367", "label-761")
        assertEquals(PrimarySortationAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newPrimarySortationAssignment("code-367", "label-761")
        try {
            item.moveTo(PrimarySortationAssignmentStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimarySortationAssignmentStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimarySortationAssignment("code-367", "label-761")
        item.status = PrimarySortationAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(PrimarySortationAssignmentStatus.DRAFT) }
    }
}
