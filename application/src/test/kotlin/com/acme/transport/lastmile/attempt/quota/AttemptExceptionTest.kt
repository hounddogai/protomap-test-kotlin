package com.acme.transport.lastmile.attempt.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newAttemptException("code-292", "title-207")
        assertEquals(AttemptExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAttemptException("code-292", "title-207")
        try {
            item.moveTo(AttemptExceptionStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptExceptionStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAttemptException("code-292", "title-207")
        item.status = AttemptExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(AttemptExceptionStatus.REQUESTED) }
    }
}
