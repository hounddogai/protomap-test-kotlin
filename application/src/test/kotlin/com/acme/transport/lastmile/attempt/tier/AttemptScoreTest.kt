package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptScoreTest {
    @Test
    fun `starts in new`() {
        val item = newAttemptScore("code-924", "name-359")
        assertEquals(AttemptScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAttemptScore("code-924", "name-359")
        try {
            item.moveTo(AttemptScoreStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptScoreStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAttemptScore("code-924", "name-359")
        item.status = AttemptScoreStatus.FAILED
        assertFails("move") { item.moveTo(AttemptScoreStatus.NEW) }
    }
}
