package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newCutoffAssignment("code-178", "title-285")
        assertEquals(CutoffAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCutoffAssignment("code-178", "title-285")
        try {
            item.moveTo(CutoffAssignmentStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffAssignmentStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCutoffAssignment("code-178", "title-285")
        item.status = CutoffAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(CutoffAssignmentStatus.REQUESTED) }
    }
}
