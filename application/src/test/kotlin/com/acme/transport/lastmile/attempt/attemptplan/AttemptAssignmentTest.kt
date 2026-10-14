package com.acme.transport.lastmile.attempt.attemptplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newAttemptAssignment("code-154", "title-509")
        assertEquals(AttemptAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAttemptAssignment("code-154", "title-509")
        try {
            item.moveTo(AttemptAssignmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptAssignmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAttemptAssignment("code-154", "title-509")
        item.status = AttemptAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AttemptAssignmentStatus.PROPOSED) }
    }
}
