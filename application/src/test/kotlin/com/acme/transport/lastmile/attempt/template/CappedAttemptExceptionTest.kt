package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAttemptExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newCappedAttemptException("code-836", "name-870")
        assertEquals(CappedAttemptExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newCappedAttemptException("code-836", "name-870")
        try {
            item.moveTo(CappedAttemptExceptionStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAttemptExceptionStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedAttemptException("code-836", "name-870")
        item.status = CappedAttemptExceptionStatus.FAILED
        assertFails("move") { item.moveTo(CappedAttemptExceptionStatus.NEW) }
    }
}
