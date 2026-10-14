package com.acme.transport.customs.hold.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newHoldAssignment("code-593", "title-737")
        assertEquals(HoldAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newHoldAssignment("code-593", "title-737")
        try {
            item.moveTo(HoldAssignmentStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldAssignmentStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHoldAssignment("code-593", "title-737")
        item.status = HoldAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(HoldAssignmentStatus.DRAFT) }
    }
}
