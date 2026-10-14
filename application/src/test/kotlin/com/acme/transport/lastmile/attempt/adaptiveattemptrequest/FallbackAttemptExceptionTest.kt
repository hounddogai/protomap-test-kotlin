package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAttemptExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackAttemptException("code-213", "label-40")
        assertEquals(FallbackAttemptExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackAttemptException("code-213", "label-40")
        try {
            item.moveTo(FallbackAttemptExceptionStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAttemptExceptionStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackAttemptException("code-213", "label-40")
        item.status = FallbackAttemptExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackAttemptExceptionStatus.REQUESTED) }
    }
}
