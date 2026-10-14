package com.acme.transport.lastmile.attempt.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAttemptScoreTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryAttemptScore("code-427", "name-315")
        assertEquals(PrimaryAttemptScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newPrimaryAttemptScore("code-427", "name-315")
        try {
            item.moveTo(PrimaryAttemptScoreStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAttemptScoreStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryAttemptScore("code-427", "name-315")
        item.status = PrimaryAttemptScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryAttemptScoreStatus.OPEN) }
    }
}
