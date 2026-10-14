package com.acme.transport.lastmile.locker.lockerassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLockerAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveLockerAssignment("code-19", "name-939")
        assertEquals(AdaptiveLockerAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newAdaptiveLockerAssignment("code-19", "name-939")
        try {
            item.moveTo(AdaptiveLockerAssignmentStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLockerAssignmentStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveLockerAssignment("code-19", "name-939")
        item.status = AdaptiveLockerAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveLockerAssignmentStatus.DRAFT) }
    }
}
