package com.acme.transport.lastmile.attempt.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptProfileTest {
    @Test
    fun `starts in open`() {
        val item = newAttemptProfile("code-726", "name-83")
        assertEquals(AttemptProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAttemptProfile("code-726", "name-83")
        try {
            item.moveTo(AttemptProfileStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptProfileStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAttemptProfile("code-726", "name-83")
        item.status = AttemptProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(AttemptProfileStatus.OPEN) }
    }
}
