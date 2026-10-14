package com.acme.transport.lastmile.attempt.attemptdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAttemptAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newManualAttemptAssignment("code-416", "title-508")
        assertEquals(ManualAttemptAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualAttemptAssignment("code-416", "title-508")
        try {
            item.moveTo(ManualAttemptAssignmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAttemptAssignmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualAttemptAssignment("code-416", "title-508")
        item.status = ManualAttemptAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(ManualAttemptAssignmentStatus.CREATED) }
    }
}
